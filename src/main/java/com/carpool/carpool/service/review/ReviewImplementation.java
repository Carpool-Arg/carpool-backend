package com.carpool.carpool.service.review;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.carpool.carpool.dto.review.ReviewPassengerRequestDTO;
import com.carpool.carpool.enums.reservation.ReservationStateEnum;
import com.carpool.carpool.model.stateHistory.StateHistory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carpool.carpool.dto.review.ReviewDriverRequestDTO;
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

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public Response<ReviewResponseDTO> createPassengerReview(ReviewPassengerRequestDTO reviewPassengerRequestDTO) {
        User reviewerUser = GetAuthenticatedUser();

        driverRepository.findByUserId(reviewerUser.getId())
                .orElseThrow(
                        () -> new ConflictException("No se encontró el perfil de chofer para el usuario autenticado."));

        User targetUser = userRepository.findByIdAndDeletedAtIsNull(reviewPassengerRequestDTO.getPassengerId())
                .orElseThrow(() -> new ConflictException("Pasajero a reseñar no encontrado."));

        //La reseña se puede hacer solamente si el viaje finalizó.
        Trip trip = tripRepository.findById(reviewPassengerRequestDTO.getTripId())
                .orElseThrow(() -> {
                    log.error("Error: Viaje ID {} no encontrado", reviewPassengerRequestDTO.getTripId());
                    return new ResourceNotFoundException("Viaje no encontrado");
                });

        if (!stateHistoryRepository.isCurrentState(trip, "FINISHED", ScopeEnum.TRIP)) {
            log.warn("Intento de reseña fallido: El viaje {} no está FINISHED", trip.getId());
            throw new ConflictException("Solo se puede reseñar un viaje que haya finalizado.");
        }

        //Verificar que el id del targetUser que se envia tenga una reserva asociada a dicho viaje
        List<String> excludedStates = List.of(ReservationStateEnum.REJECTED.name(), ReservationStateEnum.CANCELLED.name());

        reservationRepository.findReservationByUserAndTripExcludingStates(targetUser.getId(), trip.getId(), excludedStates)
                .orElseThrow(() -> {
                    log.error("Error: No se encontró una reserva con estado válido para el usuario {} en el viaje {}", targetUser.getId(), trip.getId());
                    return new ConflictException("El usuario que se quiere reseñar no tiene una reserva con estado válido asociada a este viaje.");
                });

        // La reseña se puede realizar hasta 48hs luego de que el viaje haya finalizado.
        StateHistory finishedState = stateHistoryRepository
                .findTopByTripAndState_NameAndState_ScopeOrderByStartDateTimeDesc(
                        trip,
                        "FINISHED",
                        ScopeEnum.TRIP
                )
                .orElseThrow(() -> new IllegalStateException("No se encontró el estado FINISHED del viaje."));

        LocalDateTime finishedAt = finishedState.getStartDateTime();
        LocalDateTime now = LocalDateTime.now();

        if (finishedAt.plusHours(48).isBefore(now)) {
            log.warn("Intento de reseña fuera de plazo: viaje {} finalizado en {}", trip.getId(), finishedAt);
            throw new ConflictException("La reseña solo puede realizarse dentro de las 48 horas posteriores a la finalización del viaje.");
        }

        //Verificar que el pasajero enviado no haya sido reseñado
        if (reviewRepository.existsByTargetUserIdAndTripId(targetUser.getId(), trip.getId())) {
            log.warn("Intento de crear reseña duplicada: El usuario {} ya ha sido reseñado para el viaje {}", targetUser.getId(), trip.getId());
            throw new ConflictException("Ya has realizado una reseña para el pasajero ingresado.");
        }

        if (moderationService.isToxic(reviewPassengerRequestDTO.getDescription())) {
            log.warn("Reseña bloqueada por contenido ofensivo (IA). Usuario: {}", reviewerUser.getUsername());
            throw new ConflictException("Tu comentario ha sido detectado como ofensivo. Por favor, mantén el respeto.");
        }

        Review review = Review.builder()
                .stars(reviewPassengerRequestDTO.getStars())
                .description(reviewPassengerRequestDTO.getDescription())
                .reviewerUser(reviewerUser)
                .targetUser(targetUser)
                .trip(trip)
                .passengerToDriver(false)
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("Reseña guardada exitosamente con ID: {}", savedReview.getId());

        updateUserRating(targetUser.getId(), false);

        return ResponseUtils.buildOKResponse(
                List.of("Reseña creada con éxito"),
                reviewMapper.convertReviewToReviewResponseDTO(savedReview)
        );
    }

    @Override
    @Transactional
    public Response<ReviewResponseDTO> createDriverReview(ReviewDriverRequestDTO reviewDriverRequestDTO) {

        User userReviewer = GetAuthenticatedUser();

        Trip trip = tripRepository.findById(reviewDriverRequestDTO.getTripId())
                .orElseThrow(() -> {
                    log.error("Error: Viaje ID {} no encontrado", reviewDriverRequestDTO.getTripId());
                    return new ResourceNotFoundException("Viaje no encontrado");
                });

        if (!stateHistoryRepository.isCurrentState(trip, "FINISHED", ScopeEnum.TRIP)) {
            log.warn("Intento de reseña fallido: El viaje {} no está FINISHED", trip.getId());
            throw new ConflictException("Solo se puede reseñar un viaje que haya finalizado.");
        }

        // La reseña se puede realizar hasta 48hs luego de que el viaje haya finalizado.
        StateHistory finishedState = stateHistoryRepository
                .findTopByTripAndState_NameAndState_ScopeOrderByStartDateTimeDesc(
                        trip,
                        "FINISHED",
                        ScopeEnum.TRIP
                )
                .orElseThrow(() -> new IllegalStateException("No se encontró el estado FINISHED del viaje."));

        LocalDateTime finishedAt = finishedState.getStartDateTime();
        LocalDateTime now = LocalDateTime.now();

        if (finishedAt.plusHours(48).isBefore(now)) {
            log.warn("Intento de reseña fuera de plazo: viaje {} finalizado en {}", trip.getId(), finishedAt);
            throw new ConflictException("La reseña solo puede realizarse dentro de las 48 horas posteriores a la finalización del viaje.");
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

        if (moderationService.isToxic(reviewDriverRequestDTO.getDescription())) {
            log.warn("Reseña bloqueada por contenido ofensivo (IA). Usuario: {}", userReviewer.getUsername());
            throw new ConflictException("Tu comentario ha sido detectado como ofensivo. Por favor, mantén el respeto.");
        }

        User targetUser = trip.getVehicle().getDriver().getUser();

        Review review = Review.builder()
                .stars(reviewDriverRequestDTO.getStars())
                .description(reviewDriverRequestDTO.getDescription())
                .reviewerUser(userReviewer)
                .targetUser(targetUser)
                .trip(trip)
                .passengerToDriver(true)
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("Reseña guardada exitosamente con ID: {}", savedReview.getId());

        updateUserRating(targetUser.getId(), true);

        return ResponseUtils.buildOKResponse(
                List.of("Reseña creada con éxito"),
                reviewMapper.convertReviewToReviewResponseDTO(savedReview)
        );
    }
    @Override
    public Response<Boolean> canDriverReviewTrip(Long tripId, Long passengerId) {
        User user = GetAuthenticatedUser();

        Driver driver = driverRepository.findByUserId(user.getId())
                .orElseThrow(
                        () -> new ConflictException("No se encontró el perfil de chofer para el usuario autenticado."));

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> {
                    log.error("Error: No se encontró el viaje con ID {}", tripId);
                    return new ResourceNotFoundException("Viaje no encontrado");
                });

        User targetUser = userRepository.findByIdAndDeletedAtIsNull(passengerId)
                .orElseThrow(() -> new ConflictException("Pasajero a reseñar no encontrado."));

        boolean isFinished = stateHistoryRepository.isCurrentState(trip, "FINISHED", ScopeEnum.TRIP);

        //El chofer en sesion está intentando reseñar un viaje que NO le pertenece
        if (!Objects.equals(trip.getVehicle().getDriver().getId(), driver.getId())){
            String msg = "El usuario no está habilitado para reseñar este viaje.";
            log.warn("Check canDriverReviewTrip - Fallo: {}, Usuario: {}", msg, user.getUsername());
            return ResponseUtils.buildOKResponse(List.of(msg), false);
        }

        //El viaje NO está finalziado
        if (!isFinished) {
            String msg = "El usuario no está habilitado para reseñar este viaje. El mismo no está finalizado";
            log.warn("Check canDriverReviewTrip - Fallo: {}, Usuario: {}", msg, user.getUsername());
            return ResponseUtils.buildOKResponse(List.of(msg), false);
        }

        //Verificar que el pasajero enviado no haya sido reseñado
        if (reviewRepository.existsByTargetUserIdAndTripId(targetUser.getId(), trip.getId())) {
            String msg = "El usuario no está habilitado para reseñar este viaje. El pasajero ya fue reseñado";
            log.warn("Check canDriverReviewTrip - Fallo: {}, Usuario: {}", msg, user.getUsername());
            return ResponseUtils.buildOKResponse(List.of(msg), false);
        }

        //Verificar que el id del targetUser que se envia tenga una reserva asociada a dicho viaje
        List<String> excludedStates = List.of(
                ReservationStateEnum.PENDING.name(),
                ReservationStateEnum.ACCEPTED.name(),
                ReservationStateEnum.IN_PROGRESS.name(),
                ReservationStateEnum.REJECTED.name(),
                ReservationStateEnum.CANCELLED.name()
        );

        Optional<Reservation> reservationOpt = reservationRepository
                .findReservationByUserAndTripExcludingStates(targetUser.getId(), trip.getId(), excludedStates);

        if (reservationOpt.isEmpty()) {
            String msg = "El usuario no está habilitado para reseñar este viaje. El pasajero no tiene asociada una reserva a dicho viaje";
            log.warn("Check canDriverReviewTrip - Fallo: {}, Usuario: {}", msg, user.getUsername());
            return ResponseUtils.buildOKResponse(List.of(msg), false);
        }

        // La reseña se puede realizar hasta 48hs luego de que el viaje haya finalizado.
        StateHistory finishedState = stateHistoryRepository
                .findTopByTripAndState_NameAndState_ScopeOrderByStartDateTimeDesc(
                        trip,
                        "FINISHED",
                        ScopeEnum.TRIP
                )
                .orElseThrow(() -> new IllegalStateException("No se encontró el estado FINISHED del viaje."));

        LocalDateTime finishedAt = finishedState.getStartDateTime();
        LocalDateTime now = LocalDateTime.now();

        if (finishedAt.plusHours(48).isBefore(now)) {
            String msg = "El usuario no está habilitado para reseñar este viaje. La reseña solo puede realizarse dentro de las 48 horas posteriores a la finalización del viaje.";
            log.warn("Check canDriverReviewTrip - Fallo: {}, Usuario: {}", msg, user.getUsername());
            return ResponseUtils.buildOKResponse(List.of(msg), false);
        }

        String msg = "Usuario habilitado para dejar su reseña sobre el viaje.";
        log.info("Check canReview - Éxito: {}, Usuario: {}", msg, user.getUsername());
        return ResponseUtils.buildOKResponse(List.of(msg), true);
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
     * Recalcula y actualiza el rating promedio del usuario reseñado. Se llama después de crear una nueva reseña para asegurar que el rating esté siempre actualizado.
     * @param userId ID del usuario target  para el cual se desea actualizar el rating promedio
     * @param passengerToDriver Indica si la reseña fue realizada por un pasajero hacia un chofer.
     *  *                          Si es {@code true}, se actualiza el perfil de {@link com.carpool.carpool.model.driver.Driver}.
     *  *                          Si es {@code false}, se actualiza el {@link com.carpool.carpool.model.user.User}.
     * @throws ResourceNotFoundException si no se encuentra el perfil asociado al usuario
     */
    private void updateUserRating(Long userId, boolean passengerToDriver) {
        log.debug("Recalculando rating para el usuario ID: {}", userId);

        RatingStats stats = getRatingStats(userId);
        double roundedAverage = calculateAverageWithBase(stats);

        if (passengerToDriver) {
            updateDriverRating(userId, stats, roundedAverage);
        } else {
            updatePassengerRating(userId, stats, roundedAverage);
        }
    }

    /**
     * Obtiene las estadísticas de reseñas para un usuario.
     *
     * <p>Realiza una consulta al repo que retorna:
     * <ul>
     *     <li>Cantidad total de reseñas</li>
     *     <li>Suma total de estrellas</li>
     * </ul>
     *
     * @param userId ID del usuario del cual se desean obtener estadísticas.
     * @return {@link RatingStats} con cantidad de reseñas y suma de estrellas.
     */
    private RatingStats getRatingStats(Long userId) {
        Object[] result = (Object[]) reviewRepository.getReviewStatsByUserId(userId);

        long count = result[0] != null ? ((Number) result[0]).longValue() : 0L;
        double sum = result[1] != null ? ((Number) result[1]).doubleValue() : 0.0;

        return new RatingStats(count, sum);
    }

    /**
     * Calcula el promedio de rating aplicando la política de base 5.0.
     *
     * <p>La política consiste en:
     * <ul>
     *     <li>Sumar una reseña base de 5 estrellas</li>
     *     <li>Incrementar el conteo total en 1</li>
     * </ul>
     *
     * El resultado se redondea a un decimal.
     *
     * @param stats Estadísticas actuales del usuario.
     * @return Promedio final redondeado a un decimal.
     */
    private double calculateAverageWithBase(RatingStats stats) {
        double totalSum = 5.0 + stats.sum();
        long totalCount = 1 + stats.count();

        double average = totalSum / totalCount;
        return Math.round(average * 10.0) / 10.0;
    }

    /**
     * Actualiza el rating promedio del perfil de chofer asociado al usuario.
     *
     * @param userId ID del usuario target.
     * @param stats Estadísticas de reseñas utilizadas para logging.
     * @param average Promedio calculado y redondeado.
     *
     * @throws ResourceNotFoundException si no se encuentra el perfil de chofer.
     */
    private void updateDriverRating(Long userId, RatingStats stats, double average) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de chofer no encontrado"));

        log.info("Rating Chofer {}. Reseñas: {}. Suma: {}. Promedio: {}",
                userId, stats.count(), stats.sum(), average);

        driver.setRating(average);
        driverRepository.save(driver);
    }

    /**
     * Actualiza el rating promedio del pasajero (entidad User).
     *
     * @param userId ID del usuario target.
     * @param stats Estadísticas de reseñas utilizadas para logging.
     * @param average Promedio calculado y redondeado.
     *
     * @throws ConflictException si no se encuentra el pasajero.
     */
    private void updatePassengerRating(Long userId, RatingStats stats, double average) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ConflictException("Pasajero no encontrado."));

        log.info("Rating Pasajero {}. Reseñas: {}. Suma: {}. Promedio: {}",
                userId, stats.count(), stats.sum(), average);

        user.setRating(average);
        userRepository.save(user);
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

    private record RatingStats(long count, double sum) {}
}

