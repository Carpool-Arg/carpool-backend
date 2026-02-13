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

    // private static final List<String> FORBIDDEN_WORDS = List.of(
    //     "boludo", "pelotudo", "otario", "otario de mierda", "gil",
    //     "forro", "conchudo", "conchuda", "papudo", "papuda", "paporroto", "garca", "careta", "chanta", "turro",
    //     "sorete", "sorete con patas", "pedazo de mierda", "mierda", "kpo", "kpo de mierda",
    //     "hijo de puta", "hdp", "hijo de re mil puta", "la puta que te parió", "la puta que te remil parió", "la puta madre",
    //     "la concha de tu madre", "la concha tuya", "la concha de tu hermana", "la concha de la lora", "uh la concha de la yuta",
    //     "culo", "culeado", "qué culeado", "culeadazo", "orto", "que te den por el orto", "metételo en el orto",
    //     "chupamedias", "chupapijas",
    //     "gorreado", "no seas gorreado", "chomazo", "chomi", "ocote", "da ocote", "qué ocote", "chivazo", "mocazo",
    //     "pijudo", "garcha", "mamón", "reventado", "trucho",
    //     "¡andá a cagar, forro!", "¡qué pelotudo sos, loco!", "¡sos un sorete, eh!", "¡qué garca sos!", 
    //     "¡otario, aprendé!", "¡papudo de mierda!", "¡no seas tan culeado!"
    // );

    @Override
    @Transactional
    public Response<ReviewResponseDTO> createReview(ReviewRequestDTO reviewRequestDTO) {
        log.info("Iniciando creación de reseña para el viaje ID: {}", reviewRequestDTO.getTripId());

        User userReviewer = GetAuthenticatedUser();
        log.debug("Usuario reseñador identificado: {}", userReviewer.getUsername());

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
             .orElseThrow(() -> new ConflictException("No tienes una reserva asociada a este viaje."));

        if (!stateHistoryRepository.isCurrentStateReservation(reservation, "COMPLETED", ScopeEnum.RESERVATION)) {
            log.warn("Reserva {} no está COMPLETED", reservation.getId());
            throw new ConflictException("Solo se puede reseñar un viaje que hayas completado y abonado.");
        }

        log.debug("Validaciones exitosas. Aplicando filtro de palabras a la descripción.");
        

        if (moderationService.isToxic(reviewRequestDTO.getDescription())) {
            log.warn("Reseña bloqueada por contenido ofensivo (IA). Usuario: {}", userReviewer.getUsername());
            throw new ConflictException("Tu comentario ha sido detectado como ofensivo. Por favor, mantén el respeto.");
        }

        log.debug("Validaciones de seguridad y contenido exitosas.");

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
    public Response<List<ReviewResponseDTO>> getReviewsByTargetUser(Long targetUserId) {
        log.info("Consultando reseñas para el usuario target ID: {}", targetUserId);
        
        if (!userRepository.existsById(targetUserId)) {
            log.error("Error: Usuario target ID {} no existe", targetUserId);
            throw new ResourceNotFoundException("Usuario no encontrado");
        }

        List<Review> reviews = reviewRepository.findByTargetUserIdAndDeletedAtIsNull(targetUserId);
        log.debug("Se encontraron {} reseñas para el usuario {}", reviews.size(), targetUserId);

        List<ReviewResponseDTO> responseList = reviews.stream()
                .map(reviewMapper::convertReviewToReviewResponseDTO)
                .toList();

        return ResponseUtils.buildOKResponse(List.of("Reseñas obtenidas con éxito"), responseList);
    }

    @Override
    public Response<Boolean> canUserReviewTrip(Long tripId) {
        User user = GetAuthenticatedUser();
        
        
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Viaje no encontrado"));


        boolean belongsToTrip = reservationRepository.existsByUserIdAndTripId(user.getId(), tripId);
        
        boolean isFinished = stateHistoryRepository.isCurrentState(trip, "FINISHED", ScopeEnum.TRIP);

        boolean result = belongsToTrip && isFinished;

        log.info("Verificación canReview: Usuario {}, Viaje {}, Resultado: {}", 
                user.getUsername(), tripId, result);
        
        return ResponseUtils.buildOKResponse(List.of("Usuario habilitado para dejar su reseña sobre el viaje " + tripId), result);
    }

    /**
     * Recalcula y actualiza el rating promedio del chofer asociado al usuario dado. Se llama después de crear una nueva reseña para asegurar que el rating del chofer esté siempre actualizado.
     * @param userId ID del usuario target (chofer) para el cual se desea actualizar el rating promedio
     * @throws ResourceNotFoundException si no se encuentra el perfil de chofer asociado al usuario
     */
    private void updateDriverRating(Long userId) {
        log.debug("Recalculando rating para el chofer asociado al usuario ID: {}", userId);
        Double average = reviewRepository.getAverageRatingByUserId(userId);
        
        if (average != null) {
            double roundedAverage = Math.round(average * 10.0) / 10.0;
            
            Driver driver = driverRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Perfil de chofer no encontrado"));
            
            log.info("Actualizando rating del chofer {}. Anterior: {}, Nuevo: {}", userId, driver.getRating(), roundedAverage);
            driver.setRating(roundedAverage);
            driverRepository.save(driver);
        } else {
            log.warn("No se pudo calcular el promedio para el usuario ID: {} (average es null)", userId);
        }
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

    // /**
    //  * Reemplaza palabras prohibidas en el texto con asteriscos. La comparación es case-insensitive.
    //  * @param text El texto a filtrar
    //  * @return El texto filtrado, con las palabras prohibidas reemplazadas por "***". Si el texto es null o está en blanco, se devuelve sin cambios.
    //  */
    // private String filterBadWords(String text) {
    //     if (text == null || text.isBlank()) return text;
    //     String filtered = text;
    //     for (String word : FORBIDDEN_WORDS) {
    //         filtered = filtered.replaceAll("(?i)" + word, "***");
    //     }
    //     return filtered;
    // }
}