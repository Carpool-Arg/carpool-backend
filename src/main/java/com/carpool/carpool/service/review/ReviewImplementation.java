package com.carpool.carpool.service.review;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carpool.carpool.dto.review.ReviewRequestDTO;
import com.carpool.carpool.dto.review.ReviewResponseDTO;
import com.carpool.carpool.enums.state.ScopeEnum;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.mappers.review.ReviewMapper;
import com.carpool.carpool.model.driver.Driver;
import com.carpool.carpool.model.reservation.Reservation;
import com.carpool.carpool.model.review.Review;
import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.driver.DriverRepository;
import com.carpool.carpool.repository.reservation.ReservationRepository;
import com.carpool.carpool.repository.review.ReviewRepository;
import com.carpool.carpool.repository.stateHistory.StateHistoryRepository;
import com.carpool.carpool.repository.trip.TripRepository;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.utils.ResponseUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Service
@RequiredArgsConstructor
public class ReviewImplementation implements IReviewService {

    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final ReviewRepository reviewRepository;
    private final StateHistoryRepository stateHistoryRepository;
    private final ReservationRepository reservationRepository;
    private final DriverRepository driverRepository;
    private final ReviewMapper reviewMapper;
    private final ModerationService moderationService;

    @Override
    @Transactional
    public Response<ReviewResponseDTO> createReview(ReviewRequestDTO reviewRequestDTO) {

        User userReviewer = GetAuthenticatedUser();

        Trip trip = tripRepository.findById(reviewRequestDTO.getTripId())
                .orElseThrow(() -> {
                    log.error("Error: Viaje ID {} no encontrado", reviewRequestDTO.getTripId());
                    return new ResourceNotFoundException("Viaje no encontrado");
                });

        if (!stateHistoryRepository.isCurrentState(trip, "FINISHED", ScopeEnum.TRIP)) {
            log.warn("Intento de reseña fallido: El viaje {} no está FINISHED", trip.getId());
            throw new ConflictException("Solo se puede reseñar un viaje que haya finalizado.");
        }

        if (reviewRepository.existsByReviewerUserIdAndTripId(userReviewer.getId(), trip.getId())) {
            log.warn("Intento de crear reseña duplicada: El usuario {} ya ha reseñado el viaje {}", userReviewer.getId(), trip.getId());
            throw new ConflictException("Ya has realizado una reseña para este viaje.");
        }

        Reservation reservation = reservationRepository.findReservationByUserAndTrip(userReviewer.getId(), trip.getId())
                .orElseThrow(() -> {
                    log.error("Error: No se encontró una reserva para el usuario {} en el viaje {}", userReviewer.getId(), trip.getId());
                    return new ConflictException("No tienes una reserva asociada a este viaje.");
                });

        if (!stateHistoryRepository.isCurrentStateReservation(reservation, "COMPLETED", ScopeEnum.RESERVATION)) {
            log.warn("Reserva {} no está COMPLETED", reservation.getId());
            throw new ConflictException("Solo se puede reseñar un viaje que hayas completado y abonado.");
        }

    
        if (moderationService.isToxic(reviewRequestDTO.getDescription())) {
            log.warn("Reseña bloqueada por contenido ofensivo (IA). Usuario: {}", userReviewer.getUsername());
            throw new ConflictException("Tu comentario ha sido detectado como ofensivo. Por favor, mantén el respeto.");
        }

        User targetUser = trip.getVehicle().getDriver().getUser();

        Review review = Review.builder()
                .stars(reviewRequestDTO.getStars())
                .description(reviewRequestDTO.getDescription())
                .reviewerUser(userReviewer)
                .targetUser(targetUser)
                .trip(trip)
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("Reseña guardada exitosamente con ID: {}", savedReview.getId());

        updateDriverRating(targetUser.getId());

        return ResponseUtils.buildOKResponse(
                List.of("Reseña creada con éxito"),
                reviewMapper.convertReviewToReviewResponseDTO(savedReview)
        );
    }

    @Override
    public Response<Boolean> canUserReviewTrip(Long tripId) {
        User user = GetAuthenticatedUser();
        
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> {
                    log.error("Error: No se encontró el viaje con ID {}", tripId);
                    return new ResourceNotFoundException("Viaje no encontrado");
                });

        boolean belongsToTrip = reservationRepository.existsByUserIdAndTripId(user.getId(), tripId);
        boolean isFinished = stateHistoryRepository.isCurrentState(trip, "FINISHED", ScopeEnum.TRIP);
        boolean alreadyReviewed = reviewRepository.existsByReviewerUserIdAndTripId(user.getId(), tripId);

        // Usuario no pertence al viaje o no existe 
        if (!belongsToTrip || !isFinished) {
            String msg = "El usuario no está habilitado para reseñar este viaje.";
            log.warn("Check canReview - Fallo: {}, Usuario: {}", msg, user.getUsername());
            return ResponseUtils.buildOKResponse(List.of(msg), false);
        }

        // El usuario ya reseñó este viaje 
        if (alreadyReviewed) {
            String msg = "El usuario ya ha dejado una reseña para el viaje.";
            log.info("Check canReview - Fallo: {}, Usuario: {}", msg, user.getUsername());
            return ResponseUtils.buildOKResponse(List.of(msg), false);
        }

        // El usuario puede reseñar el viaje 
        String msg = "Usuario habilitado para dejar su reseña sobre el viaje.";
        log.info("Check canReview - Éxito: {}, Usuario: {}", msg, user.getUsername());
        return ResponseUtils.buildOKResponse(List.of(msg), true);
    }

    /**
     * Recalcula y actualiza el rating promedio del chofer asociado al usuario dado. Se llama después de crear una nueva reseña para asegurar que el rating del chofer esté siempre actualizado.
     * @param userId ID del usuario target (chofer) para el cual se desea actualizar el rating promedio
     * @throws ResourceNotFoundException si no se encuentra el perfil de chofer asociado al usuario
     */
    private void updateDriverRating(Long userId) {
        log.debug("Recalculando rating con base 5 para el usuario ID: {}", userId);
        
        Object result = reviewRepository.getReviewStatsByUserId(userId);
        Object[] stats = (Object[]) result;

        long count = (stats[0] != null) ? ((Number) stats[0]).longValue() : 0L;
        double sum = (stats[1] != null) ? ((Number) stats[1]).doubleValue() : 0.0;

        double totalSum = 5.0 + sum; 
        long totalCount = 1 + count;

        double average = totalSum / totalCount;
        double roundedAverage = Math.round(average * 10.0) / 10.0;

        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de chofer no encontrado"));

        log.info("Rating Chofer {}. Reseñas: {}. Suma: {}. Promedio: {}", 
                userId, count, sum, roundedAverage);

        driver.setRating(roundedAverage);
        driverRepository.save(driver);
    }

    /**
     * Obtiene el usuario autenticado actualmente en el contexto de seguridad. Si no se encuentra un usuario válido, lanza una excepción de conflicto.
     * @return  El usuario autenticado en el sistema
     * @throws ConflictException si no se encuentra un usuario válido en el contexto
     */
    private User GetAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new ConflictException("Usuario autenticado no encontrado."));
    }
}