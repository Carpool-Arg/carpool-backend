package com.carpool.carpool.service.review;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carpool.carpool.dto.review.ReviewRequestDTO;
import com.carpool.carpool.dto.review.ReviewResponseDTO;
import com.carpool.carpool.dto.review.ReviewToMeDTO;
import com.carpool.carpool.dto.review.ReviewsToMeResponseDTO;
import com.carpool.carpool.enums.state.ScopeEnum;
import com.carpool.carpool.exception.BadRequestException;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.mappers.review.ReviewMapper;
import com.carpool.carpool.model.driver.Driver;
import com.carpool.carpool.model.reservation.Reservation;
import com.carpool.carpool.model.review.Review;
import com.carpool.carpool.model.role.Role;
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

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.carpool.carpool.dto.review.DriverReviewResponseDTO;

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
    public Response<List<DriverReviewResponseDTO>> getDriverReviews(Long driverId, int skip, String orderBy) {
        log.info("Iniciando la recuperacion de las reservas del chofer con el id: {}",driverId);
        Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new ResourceNotFoundException("El chofer no existe."));

        List<Review> reviews = reviewRepository.findReviewsByTargetUser(driver.getUser().getId(),getPageable(orderBy, skip));
        
        log.info("Reseñas obtenidas con exito. Cantidad: {}",reviews.size());
        if(reviews.isEmpty()){
        return ResponseUtils.buildOKResponse(List.of("El chofer no tiene reseñas"),null); 
        }

        List<DriverReviewResponseDTO> driverReviews = reviews
        .stream()
        .map(reviewMapper::convertReviewToDriverReviewResponseDTO)
        .toList();

        
        return ResponseUtils.buildOKResponse(List.of("Reseñas recuperadas con éxito"),driverReviews); 
    }

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
                .passengerToDriver(true)
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

    @Override
    public Response<ReviewsToMeResponseDTO> getReviewsToMe(LocalDate dateFrom,LocalDate dateTo,String role,int skip,String orderBy) {

        User user = GetAuthenticatedUser();

        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;

        if (dateFrom != null) {
            fromDateTime = dateFrom.atStartOfDay();
        }
        if (dateTo != null) {
            toDateTime = dateTo.atTime(23, 59, 59);
        }
        
        log.info("Buscando reseñas para el usuario con el ID {}. Filtros: Fecha desde: {}. Fecha hasta: {}. Rol: {}. Skip: {}. Orden: {}",
            user.getId(),fromDateTime, toDateTime, role,skip,orderBy
        );

        Page<Review> page;
        Double rating;

        if ("DRIVER".equalsIgnoreCase(role)) {
            log.info("Verificando si el usuario tiene el rol de chofer.");
            if(!user.hasRole("ROLE_DRIVER")) throw new BadRequestException("El usuario no posee el rol indicado");
            try{
                rating = user.getDriver().getRating();
            }catch(Exception e){
                throw new ConflictException("Hubo un problema al recuperar el usuario.");
            }

            log.info("Recuperando pagina de reseñas que pasajeros le hicieron al usuario como chofer.");
            page = reviewRepository.findReviewsByTargetUserWithFilters(
                user.getId(),
                fromDateTime,
                toDateTime,
                true,
                getPageable(orderBy, skip)
            );

        } else if ("PASSENGER".equalsIgnoreCase(role)) {
            try{
                rating = user.getRating();
            }catch(Exception e){
                throw new ConflictException("Hubo un problema al recuperar el usuario.");
            }
            log.info("Recuperando pagina de reseñas que choferes le hicieron al usuario como pasajero.");
            page = reviewRepository.findReviewsByTargetUserWithFilters(
                user.getId(),
                fromDateTime,
                toDateTime,
                false,
                getPageable(orderBy, skip)
            );

        } else {
            throw new BadRequestException("Rol inválido");
        }        

        List<ReviewToMeDTO> reviewsToMe = page.getContent().stream()
            .map(reviewMapper::convertReviewToReviewToMeDTO)
        .toList();

        String message = reviewsToMe.isEmpty()
            ? "No se encontraron reseñas"
            : "Reseñas recuperadas con éxito";

        ReviewsToMeResponseDTO response = ReviewsToMeResponseDTO.builder()
            .total(page.getTotalElements())
            .rating(rating)
            .reviews(reviewsToMe)
        .build();

        return ResponseUtils.buildOKResponse(List.of(message), response);
    }

    /**
     * Metodo para obtener el objeto que vamos a usar para el paginado
     * Definmos un tamaño de la pgina fijo 
     * @param type
     * @param skip
     * @return
     */
    private Pageable getPageable(String type, int skip) {
        final int PAGE_SIZE = 10;
        int page = skip / PAGE_SIZE;

        Sort sort = switch (type) {
            case "RATING_DESC" -> Sort.by("stars").descending();
            case "RATING_ASC"  -> Sort.by("stars").ascending();
            case "RECENT"      -> Sort.by("createdAt").descending();
            default            -> Sort.by("createdAt").descending();
        };

        return PageRequest.of(page, PAGE_SIZE, sort);
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

