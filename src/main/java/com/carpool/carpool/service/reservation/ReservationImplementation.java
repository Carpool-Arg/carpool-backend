package com.carpool.carpool.service.reservation;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.carpool.carpool.exception.BadRequestException;
import com.carpool.carpool.model.review.Review;
import com.carpool.carpool.service.pdfGenerator.IPdfGeneratorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.carpool.carpool.dto.reservation.CreateReservationRequestDTO;
import com.carpool.carpool.dto.reservation.DeleteTripPassengerRequestDTO;
import com.carpool.carpool.dto.reservation.ReservationDTO;
import com.carpool.carpool.dto.reservation.ReservationResponseDTO;
import com.carpool.carpool.dto.reservation.ReservationUpdateRequestDTO;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.enums.reservation.ReservationStateEnum;
import com.carpool.carpool.enums.state.ScopeEnum;
import com.carpool.carpool.enums.trip.TripStateEnum;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.exception.UnauthorizedException;
import com.carpool.carpool.mappers.reservation.ReservationMapper;
import com.carpool.carpool.model.province.city.City;
import com.carpool.carpool.model.reservation.Reservation;
import com.carpool.carpool.model.state.State;
import com.carpool.carpool.model.stateHistory.StateHistory;
import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.model.trip.tripStop.TripStop;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.city.CityRepository;
import com.carpool.carpool.repository.reservation.ReservationRepository;
import com.carpool.carpool.repository.reservation.ReservationSpecification;
import com.carpool.carpool.repository.state.StateRepository;
import com.carpool.carpool.repository.stateHistory.StateHistoryRepository;
import com.carpool.carpool.repository.trip.TripRepository;
import com.carpool.carpool.repository.trip.stop.TripStopRepository;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.media.IMediaService;
import com.carpool.carpool.service.notification.INotificationService;
import com.carpool.carpool.service.review.ModerationService;
import com.carpool.carpool.service.state.StateTransitionService;
import com.carpool.carpool.utils.ResponseUtils;
import com.carpool.carpool.utils.TripCostUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationImplementation implements IReservationService {

    private final TripRepository tripRepository;
    private final StateHistoryRepository stateHistoryRepository;
    private final ReservationMapper reservationMapper;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final TripStopRepository tripStopRepository;
    private final StateRepository stateRepository;
    private final INotificationService notificationService;
    private final IMediaService mediaService;
    private final CityRepository cityRepository;
    private final StateTransitionService stateTransitionService;
    private final ModerationService moderationService;
    private final IPdfGeneratorService pdfGeneratorService;

    private static final String RESERVATION_NOT_FOUND_MESSAGE = "Reserva no encontrada.";
    private static final String CREATED_AT = "createdAt";

    @Override
    public Response<ReservationResponseDTO> getReservation(Long idTrip, Long idStartCity, Long idDestinationCity,
            Boolean baggage, String nameState, int page, int size) {
        User driver = getAuthenticatedActiveUser();

        Specification<Reservation> filter = ReservationSpecification.byFilter(idTrip, idStartCity, idDestinationCity,
                baggage, nameState, driver.getId());
        Pageable pageable = PageRequest.of(page, size, Sort.by(CREATED_AT).descending());

        Page<Reservation> reservations = reservationRepository.findAll(filter, pageable);
        if (reservations.getTotalElements() == 0) {
            return ResponseUtils.buildOKResponse(List.of("No existen reservas para el viaje correspondiente"), null);
        }

        Map<Long, StateHistory> stateHistoryMap = reservations.getContent().stream()
                .collect(Collectors.toMap(
                        Reservation::getId,
                        reservation -> stateHistoryRepository
                                .findByReservationIdAndFinishDateTimeIsNull(reservation.getId())
                                .orElse(null)));

        // Se procede a buscar las imagenes de perfil de cada pasajero que realizo la
        // reserva al viaje
        Map<Long, String> urlImagesUsers = reservations.stream()
                .collect(Collectors.toMap(
                        reservation -> reservation.getUser().getId(),
                        reservation -> mediaService.getProfilePictureUrlByUserId(reservation.getUser().getId())));

        List<ReservationDTO> listReservation = ReservationMapper.convertReservationToReservationDTO(reservations,
                urlImagesUsers, stateHistoryMap);
        ReservationResponseDTO responseReservation = new ReservationResponseDTO();
        responseReservation.setReservation(listReservation);

        return ResponseUtils.buildOKResponse(List.of("Reservas realizadas al viaje obtenido con éxito"),
                responseReservation);
    }

    @Override
    public Response<ReservationResponseDTO> getMyReservation(String state, LocalDate dateFrom, LocalDate dateTo, int skip, String orderBy) {
        User user = getAuthenticatedActiveUser();

        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new BadRequestException("La fecha 'desde' no puede ser superior a la fecha 'hasta'.");
        }

        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;

        if (dateFrom != null) {
            fromDateTime = dateFrom.atStartOfDay();
        }
        if (dateTo != null) {
            toDateTime = dateTo.atTime(23, 59, 59);
        }

        log.info("Buscando reservas solicitadas para el usuario con el ID {}. Filtros: Fecha desde: {}. Fecha hasta: {}. Estado: {}. Skip: {}. Orden: {}",
                user.getId(),fromDateTime, toDateTime,skip,orderBy
        );

        Page<Reservation> page = reservationRepository.findMyReservationsWithFilters(
                state,
                user.getId(),
                fromDateTime,
                toDateTime,
                getPageable(orderBy, skip)
        );

        if (page.isEmpty()) {
            return ResponseUtils.buildOKResponse(
                    List.of("No se encontraron reservas"),
                    null
            );
        }

        Map<Long, StateHistory> stateHistoryMap = page.getContent().stream()
                .collect(Collectors.toMap(
                        Reservation::getId,
                        reservation -> stateHistoryRepository
                                .findByReservationIdAndFinishDateTimeIsNull(reservation.getId())
                                .orElse(null)
                ));

        Map<Long, String> urlImagesDrivers = page.getContent().stream()
                .map(r -> r.getTrip().getVehicle().getDriver().getUser().getId())
                .distinct()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> Optional.ofNullable(
                                mediaService.getProfilePictureUrlByUserId(id)
                        ).orElse("")
                ));

        List<ReservationDTO> list = ReservationMapper
                .convertMyReservationsToReservationDTO(page, urlImagesDrivers, stateHistoryMap);

        ReservationResponseDTO response = new ReservationResponseDTO();

        response.setReservation(list);
        response.setTotal(page.getTotalElements());
        return ResponseUtils.buildOKResponse(
                List.of("Reservas obtenidas con éxito"),
                response
        );
    }

    @Override
    @Transactional
    public Response<Void> createReservation(CreateReservationRequestDTO createReservationRequestDTO) {
        State statePending = stateRepository.findByNameAndScope("PENDING", ScopeEnum.RESERVATION)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el estado para crear la reserva."));

        // Validaciones de viaje
        Trip trip = tripRepository.findById(createReservationRequestDTO.getTrip())
                .orElseThrow(() -> new ResourceNotFoundException("El viaje no existe"));

        tripValidations(trip);

        // Validaciones de usuario
        User userAuth = this.getAuthenticatedActiveUser();

        if (userAuth.getId().equals(trip.getVehicle().getDriver().getUser().getId())) {
            throw new ConflictException("Este viaje te pertenece, no podés realizar una reserva en él.");
        }

        Optional<Reservation> existingReservation = reservationRepository.findReservationByUserAndTrip(userAuth.getId(),
                trip.getId());

        if (existingReservation.isPresent()) {
            // Obtenemos el estado actual de esa reserva existente

            StateHistory currentSh = stateHistoryRepository
                    .findByReservationIdAndFinishDateTimeIsNull(existingReservation.get().getId())
                    .orElseThrow(() -> new ConflictException("Error al recuperar el estado de la reserva existente."));

            String currentState = currentSh.getState().getName();

            // No se permite que un viaje que fue rechazado pueda volver a ser solicitado y
            // desaparece
            if (currentState.equals("REJECTED")) {
                throw new ConflictException(
                        "Tu solicitud para este viaje fue rechazada y no puedes volver a intentarlo.");
            }

            if (!currentState.equals("CANCELLED")) {
                throw new ConflictException(
                        "Ya tenés una reserva activa (Estado: " + currentState + ") para este viaje.");
            }

            // Si llegamos acá, es CANCELLED. El flujo sigue y crea una NUEVA reserva.
            log.info("Usuario {} tenía una reserva CANCELLED, permitiendo nueva solicitud.", userAuth.getUsername());
        }

        // Validaciones de las ciudades
        TripStop[] tripStops =  cityValidations(userAuth,createReservationRequestDTO.getStartCity(), createReservationRequestDTO.getDestinationCity(), trip);

        Reservation newReservation = reservationMapper.convertReservationRequestDTOToReservation(
                createReservationRequestDTO,
                userAuth,
                trip,
                tripStops[0],
                tripStops[1],
                TripCostUtils.calculateTripTotal(tripStops[0].getCity(), tripStops[1].getCity(), trip));

        // Creacion de reserva
        StateHistory stateHistory = StateHistory.builder()
                .reservation(newReservation)
                .state(statePending)
                .build();

        reservationRepository.save(newReservation);
        stateHistoryRepository.save(stateHistory);

        this.notificationService.send(
                trip.getVehicle().getDriver().getUser(),
                NotificationEventEnum.RESERVATION_CREATED,
                newReservation);

        return ResponseUtils
                .buildOKResponse(List.of("Reserva registrada éxito, se encuentra pendiente a confirmación."), null);
    }

    @Override
    public Response<Void> updateStateReservation(ReservationUpdateRequestDTO reservationUpdateRequestDTO) {
        log.info("Iniciando actualizacion de estado de reserva");
        final Long idReservation = reservationUpdateRequestDTO.getIdReservation();
        final Reservation reservation = reservationRepository.getReferenceById(idReservation);

        boolean hasOverlappingCancelled = false;

        if(reservation == null){
        	log.error("No se pudo encontrar la reserva en la base de datos con el id: {}", idReservation);
            throw new ResourceNotFoundException("La reserva no existe");
        }

        final StateHistory lastestStateReservation = stateHistoryRepository
                .findByReservationIdAndFinishDateTimeIsNull(reservation.getId())
                .orElseThrow(() -> new ConflictException("La reserva no tiene un estado actual."));
        final State currentStateReservation = lastestStateReservation.getState();
        if (currentStateReservation.isFinish()) {
            log.error("La reserva se encuentra en un estado final: {}", currentStateReservation.getName());
            throw new ConflictException(
                    "No se puede realizar acciones a la reserva ya que se encuentra en un estado final");
        }

        final Trip trip = reservation.getTrip();

        NotificationEventEnum notification = NotificationEventEnum.RESERVATION_REJECTED;

        if(!reservationUpdateRequestDTO.isReject()){
            log.info("Iniciando el proceso para aceptar la reserva");
            hasOverlappingCancelled = acceptReservation(trip, reservation);

            if(hasOverlappingCancelled){
                notification = NotificationEventEnum.RESERVATION_ACCEPTED_WITH_OVERLAP;
            } else {
                notification = NotificationEventEnum.RESERVATION_ACCEPTED;
            }

        }else{
        	log.info("Iniciando el proceso para rechazar la reserva");
        	stateTransitionService.transition(reservation, ScopeEnum.RESERVATION, ReservationStateEnum.PENDING.name(), ReservationStateEnum.REJECTED.name());
        }

        reservationRepository.save(reservation);

        this.notificationService.send(
                reservation.getUser(),
                notification,
                reservation);

        String message = String.format(
                "Reserva %s con éxito",
                reservationUpdateRequestDTO.isReject() ? "cancelada" : "aceptada");
        return ResponseUtils.buildOKResponse(List.of(message), null);
    }

    /**
     * Metodo encargado de aceptar una reserva, realizar el cambio de estado y
     * enviar la notificacion al conductor de que el viaje ya alcanzo el cupo maximo
     *
     * @param trip        El viaje al que se le realizaron las reservas
     * @param reservation Reserva realizada al viaje
     */
    private boolean acceptReservation(Trip trip, Reservation reservation) {

        TripStop stopStartCity = reservation.getStartCity();
        TripStop stopDestinationCity = reservation.getDestinationCity();

        LocalDateTime newStart = stopStartCity.getEstimatedArrivalDateTime();
        LocalDateTime newEnd   = stopDestinationCity.getEstimatedArrivalDateTime();

        final int discountAvailableSeat = trip.getCurrentAvailableSeats() - 1;

        if(discountAvailableSeat < 0){
        	log.error("El viaje ya alcanzo el cupo maximo. Asientos disponibles: [ {} ]", discountAvailableSeat);
            throw new ConflictException("Se alcanzó el cupo disponible, no se puede aceptar la reserva.");
        }

        if(discountAvailableSeat == 0){
        	stateTransitionService.transition(trip, ScopeEnum.TRIP, TripStateEnum.CREATED.name(), TripStateEnum.CLOSED.name());

            this.notificationService.send(
                    trip.getVehicle().getDriver().getUser(),
                    NotificationEventEnum.TRIP_FULL,
                    trip);
        }
        stateTransitionService.transition(reservation, ScopeEnum.RESERVATION, ReservationStateEnum.PENDING.name(),
                ReservationStateEnum.ACCEPTED.name());
        trip.setCurrentAvailableSeats(discountAvailableSeat);
        tripRepository.save(trip);

        List<Reservation> overlappingPending = reservationRepository.findOverlappingPendingReservations(
            reservation.getUser().getId(),
            newStart,
            newEnd,
            reservation.getId()
        );

        if(!overlappingPending.isEmpty()){
            log.info("Se encontraron reservas en estado PENDING que se solapan con la reserva aceptada. Se procederá a cancelar por sistema dichas reservas.");

            for (Reservation r : overlappingPending) {
                cancelBySystem(r.getId());
            }
        }

        if(overlappingPending.size() > 0){
            log.info("Se encontraron {} reservas en estado PENDING que se solapan con la reserva aceptada. Se procederá a cancelar por sistema dichas reservas.", overlappingPending.size());
        }

        return !overlappingPending.isEmpty();
    }

    @Override
    public Response<Void> payReservation() {
        // Validaciones de usuario
        User userAuth = this.getAuthenticatedActiveUser();

        // Buscar la reserva en estado UNPAID del usuario
        Reservation reservation = reservationRepository
                .findUnpaidReservationByUserId(userAuth.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario no tiene una reserva pendiente de pago"));

        // Cambiar de estado la reserva a completed
        stateTransitionService.transition(reservation, ScopeEnum.RESERVATION, ReservationStateEnum.UNPAID.name(),
                ReservationStateEnum.COMPLETED.name());

        // Enviar email al chofer
        notificationService.send(reservation.getTrip().getVehicle().getDriver().getUser(),
                NotificationEventEnum.RESERVATION_PAID, reservation);

        notificationService.send(
                userAuth,
                NotificationEventEnum.RESERVATION_PAID_PASSENGER,
                reservation
        );

        return ResponseUtils.buildOKResponse(List.of("Pago realizado con éxito!"), null);
    }

    @Override
    public Response<Double> calculateTotal(Long idTrip, Long idStartCity, Long idDestinationCity) {
        Trip trip = tripRepository.findById(idTrip)
                .orElseThrow(() -> new ResourceNotFoundException("El viaje no existe."));

        City startCity = cityRepository.findById(idStartCity)
                .orElseThrow(() -> new ConflictException("No se pudo encontrar la ciudad de origen."));

        City destinationCity = cityRepository.findById(idDestinationCity)
                .orElseThrow(() -> new ConflictException("No se pudo encontrar la ciudad de destino."));

        return ResponseUtils.buildOKResponse(List.of("Total calculado con exito"),
                TripCostUtils.calculateTripTotal(startCity, destinationCity, trip));
    }

    @Override
    public void finishTripReservation(Reservation reservation) {
        stateTransitionService.transition(reservation, ScopeEnum.RESERVATION, ReservationStateEnum.IN_PROGRESS.name(),
                ReservationStateEnum.UNPAID.name());
        notificationService.send(reservation.getUser(), NotificationEventEnum.RESERVATION_UNPAID, reservation);

    }

    @Override
    public void startTripReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(RESERVATION_NOT_FOUND_MESSAGE));

        State inProgressState = stateRepository
                .findByNameAndScope(ReservationStateEnum.IN_PROGRESS.name(), ScopeEnum.RESERVATION)
                .orElseThrow(() -> new ResourceNotFoundException("Estado IN_PROGRESS no encontrado para RESERVATION."));

        LocalDateTime now = LocalDateTime.now();

        stateHistoryRepository.findByReservationIdAndFinishDateTimeIsNull(reservationId)
                .ifPresent(sh -> {
                    sh.setFinishDateTime(now);
                    stateHistoryRepository.save(sh);
                });

        StateHistory newHistory = StateHistory.builder()
                .reservation(reservation)
                .state(inProgressState)
                .startDateTime(now)
                .build();

        stateHistoryRepository.save(newHistory);
    }

    @Transactional
    @Override
    public Response<Void>  cancelReservationByPassenger(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(RESERVATION_NOT_FOUND_MESSAGE));

        StateHistory currentStateReservation= stateHistoryRepository.findByReservationIdAndFinishDateTimeIsNull(reservationId)
                .orElseThrow(() -> new ConflictException("Error al recuperar el estado actual de la reserva."));

        // La cancelación de la reserva sólo podrá ser realizada por el usuario que la registró.
        User user = this.getAuthenticatedActiveUser();

        if (!user.getId().equals(reservation.getUser().getId())) {
            log.warn("Usuario sin permisos para cancelar la reserva. userId={}, reservationId={}",
                    user.getId(), reservationId);
            throw new ConflictException("El usuario en sesión no tiene permisos para cancelar la reserva");
        }

        // La cancelación se podrá realizar, al menos 1 (una) hora antes del inicio del viaje.
        Trip trip = tripRepository.findById(reservation.getTrip().getId())
                .orElseThrow(() -> {
                    log.error("Viaje asociado no encontrado. tripId={}", reservation.getTrip().getId());
                    return new ResourceNotFoundException("Viaje no encontrado.");
                });

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tripStart = trip.getStartTripDateTime();

        long minutesUntilTrip = Duration.between(now, tripStart).toMinutes();

        log.info("Validando tiempo para cancelación. now={}, tripStart={}, minutosRestantes={}",
                now, tripStart, minutesUntilTrip);

        if (minutesUntilTrip < 60) {
            log.warn("Cancelación rechazada por tiempo insuficiente. reservationId={}, minutosRestantes={}",
                    reservationId, minutesUntilTrip);
            throw new ConflictException("La reserva solo puede cancelarse con al menos 1 hora de anticipación.");
        }

        // Cancelar reserva
        stateTransitionService.transition(
                reservation,
                ScopeEnum.RESERVATION,
                List.of(
                        ReservationStateEnum.PENDING.name(),
                        ReservationStateEnum.ACCEPTED.name()),
                ReservationStateEnum.CANCELLED.name());

        log.info("Reserva cancelada correctamente. reservationId={}, previousState={}, newState=CANCELLED",
                reservationId, currentStateReservation);

        // Si el viaje se encuentra cerrado por cupo completo, se tiene que abrir nuevamente para habilitar nuevas solicitudes de reservas.

        if(currentStateReservation.getState().getName().equals(ReservationStateEnum.ACCEPTED.name())){
            log.info("Reserva estaba ACCEPTED, liberando asiento. tripId={}, asientosAntes={}",
                    trip.getId(), trip.getCurrentAvailableSeats());

            trip.setCurrentAvailableSeats(trip.getCurrentAvailableSeats()+1);

            StateHistory currentStateTrip = stateHistoryRepository.findByTripIdAndFinishDateTimeIsNull(trip.getId())
                    .orElseThrow(() -> {
                        log.error("No se pudo obtener el estado actual del viaje. tripId={}", trip.getId());
                        return new ConflictException("Error al recuperar el estado del viaje de la reserva.");
                    });

            log.info("Estado actual del viaje. tripId={}, state={}",
                    trip.getId(), currentStateTrip.getState().getName());

            if (currentStateTrip.getState().getName().equals(TripStateEnum.CLOSED.name())) {
                log.info("Reabriendo viaje (CLOSED -> CREATED). tripId={}", trip.getId());

                stateTransitionService.transition(
                        trip,
                        ScopeEnum.TRIP,
                        List.of(TripStateEnum.CLOSED.name()),
                        TripStateEnum.CREATED.name()
                );
            }

            log.info("Asiento liberado correctamente. tripId={}, asientosAhora={}",
                    trip.getId(), trip.getCurrentAvailableSeats());
        }

        // El chofer será notificado si un pasajero que ya fue confirmado cancela la reserva, y el asiento del vehículo será liberado.
        if (currentStateReservation.getState().getName().equals(ReservationStateEnum.ACCEPTED.name())){
            log.info("Enviando notificación al chofer por cancelación. reservationId={}", reservationId);
            this.notificationService.send(
                    reservation.getTrip().getVehicle().getDriver().getUser(),
                    NotificationEventEnum.RESERVATION_CANCELLED_BY_PASSENGER,
                    reservation);
        }

        log.info("Finalizó cancelación de reserva por pasajero. reservationId={}", reservationId);

        return ResponseUtils.buildOKResponse(List.of("Reserva cancelada correctamente"), null);
    }

    @Override
    public void cancelReservation(Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(RESERVATION_NOT_FOUND_MESSAGE));

        stateTransitionService.transition(
                reservation,
                ScopeEnum.RESERVATION,
                List.of(
                        ReservationStateEnum.PENDING.name(),
                        ReservationStateEnum.ACCEPTED.name()),
                ReservationStateEnum.CANCELLED.name());
    }

    @Override
    public void cancelBySystem(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con ID: " + reservationId));

        State cancelledState = stateRepository
                .findByNameAndScope(ReservationStateEnum.CANCELLED.name(), ScopeEnum.RESERVATION)
                .orElseThrow(() -> new ResourceNotFoundException("Estado CANCELLED no encontrado para RESERVATION."));

        LocalDateTime now = LocalDateTime.now();

        stateHistoryRepository.findByReservationIdAndFinishDateTimeIsNull(reservationId)
                .ifPresent(sh -> {
                    sh.setFinishDateTime(now);
                    stateHistoryRepository.save(sh);
                });

        StateHistory newHistory = StateHistory.builder()
                .reservation(reservation)
                .state(cancelledState)
                .startDateTime(now)
                .build();

        stateHistoryRepository.save(newHistory);
    }

    @Override
    @Transactional
    public Response<Void> deleteTripPassenger(DeleteTripPassengerRequestDTO request) {
        log.info("Ejecutando eliminacion de un pasajero de un viaje");

        log.info("Buscando la reserva del pasajero");
        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reserva no encontrada con ID: " + request.getReservationId()));

        Trip trip = reservation.getTrip();
        StateHistory currentStateTrip = stateHistoryRepository.findByTripIdAndFinishDateTimeIsNull(trip.getId())
                .orElseThrow(() -> new ConflictException("Error al recuperar el estado del viaje de la reserva."));

        deleteTripPassengerValidations(reservation, trip, currentStateTrip);

        log.info("Liberando asiento");
        trip.setCurrentAvailableSeats(trip.getCurrentAvailableSeats() + 1);

        if (currentStateTrip.getState().getName().equals("CLOSED")) {
            log.info("Cambiando el estado del viaje");
            stateTransitionService.transition(trip, ScopeEnum.TRIP, TripStateEnum.CLOSED.name(),
                    TripStateEnum.CREATED.name());
        }

        log.info("Cambiando el estado de la reserva");
        stateTransitionService.transition(reservation, ScopeEnum.RESERVATION, ReservationStateEnum.ACCEPTED.name(),
                ReservationStateEnum.CANCELLED.name());

        if (request.getReason() != null && !request.getReason().isEmpty()) {
            if (moderationService.isToxic(request.getReason())) {
                log.warn("Comentario bloqueado por contenido ofensivo (IA)");
                throw new ConflictException(
                        "Tu comentario ha sido detectado como ofensivo. Por favor, mantén el respeto.");
            }
            reservation.setCancellationReason(request.getReason());
        }

        log.info("Guardando la reserva en la base de datos");
        reservationRepository.save(reservation);

        log.info("Enviando notificacion a pasajero");
        this.notificationService.send(
                reservation.getUser(),
                NotificationEventEnum.PASSENGER_DELETED_FROM_TRIP,
                reservation);

        return ResponseUtils.buildOKResponse(List.of("Pasajero eliminado correctamente"), null);
    }

    @Override
    public byte[] generatePaymentReceipt(Long reservationId) {
        User userAuth = this.getAuthenticatedActiveUser();

        log.info("Generando comprobante PDF para el usuario={}, reservationId={}",
                userAuth.getUsername(), reservationId);

        Reservation reservation = reservationRepository
                .findCompletedReservationByUserIdAndReservationId(userAuth.getId(), reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró una reserva completada con el ID: " + reservationId));

        return pdfGeneratorService.generatePaymentReceiptPdf(reservation);
    }

    private void deleteTripPassengerValidations(Reservation reservation, Trip trip, StateHistory currentStateTrip) {
        log.info("Validando usuario de eliminacion de pasajero");

        User user = getAuthenticatedActiveUser();
        if (trip.getVehicle().getDriver().getUser().getId() != user.getId()) {
            throw new UnauthorizedException(
                    "Solamente el chofer que publicó el viaje puede eliminar pasajeros del mismo");
        }

        log.info("Validando estado del viaje");
        if (!currentStateTrip.getState().getName().equals("CREATED")
                && !currentStateTrip.getState().getName().equals("CLOSED")) {
            throw new ConflictException("No es posible eliminar al pasajero del viaje por el estado del mismo.");
        }

        log.info("Validando horario de la eliminacion de pasajero del viaje");
        LocalDateTime startTrip = reservation.getTrip().getStartTripDateTime();
        LocalDateTime nowPlusOneHour = LocalDateTime.now().plusHours(1);

        if (startTrip.isBefore(nowPlusOneHour)) {
            throw new ConflictException(
                    "Esta acción debe realizarse con al menos una hora de antelación al inicio del viaje.");
        }

        log.info("Validando el estado de la reserva");
        StateHistory currentSh = stateHistoryRepository.findByReservationIdAndFinishDateTimeIsNull(reservation.getId())
                .orElseThrow(() -> new ConflictException("Error al recuperar el estado de la reserva existente."));

        String currentState = currentSh.getState().getName();

        if (!currentState.equals("ACCEPTED")) {
            throw new ConflictException("No es posible dar de baja al pasajero del viaje por el estado de su reserva.");
        }

    }

    /**
     * Validaciones relacionadas al viaje. Comprobamos lo siguiente:
     * - Que el viaje NO esté lleno
     * - que no esté en curso
     * - Que no esté cerrado
     * - Que no esté cancelado
     * - Que no esté finalizado
     *
     * @param trip viaje que se quiere reservar
     */
    private void tripValidations(Trip trip) {
        // 1. Validar que no esté lleno
        if (trip.getCurrentAvailableSeats() == 0) {
            throw new ConflictException("Lo sentimos, no es posible reservar ya que el viaje está lleno.");
        }
        State currentState = null;
        // 2. obtener el estado actual (sin fecha fin)
        Optional<StateHistory> currentStateOptional = stateHistoryRepository
                .findByTripAndFinishDateTimeIsNullAndReservationIdIsNull(trip);
        if (!currentStateOptional.isPresent()) {
            // 3. Si no se encuentra un estado actual, obtener el ultimo estado con fecha
            // fin
            StateHistory stateHistory = currentStateOptional
                    .orElseGet(() -> stateHistoryRepository
                            .findTopByTripAndFinishDateTimeIsNotNullOrderByFinishDateTimeDesc(trip)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "No se encontró un estado válido para el viaje")));

            currentState = stateHistory.getState();
        } else {
            currentState = currentStateOptional.get().getState();
        }
        // 4. validar estados
        if (!currentState.getName().equals("CREATED")) {
            throw new ConflictException("No es posible reservar el viaje, debido a su estado actual.");
        }
    }

    /**
     * Validaciones relacionadas a las ciudades. Comprobamos lo siguiente:
     * - Que el viaje NO esté lleno
     * - La localidad origen y destino no pueden ser iguales. LISTO
     * - La localidad origen y destino deben existir en la tabla TripStop, en base
     * al trip que se envia por la request
     * - Que la localidad origen y destino que se pasan, respeten el orden
     * establecido en TripStop
     *
     * @param startCity       ciduad origen
     * @param destinationCity ciduad destino
     * @param trip            viaje para el cual se solicita la reserva
     * @return TripStop[] Arreglo con los TripStops correspondientes a las ciudades
     *         de origen y destino válidas.
     * @throws ConflictException si las ciudades son iguales, si no existen en
     *                           tripStop o no respetan el orden.
     */
    private TripStop[] cityValidations(User user,Long startCity, Long destinationCity, Trip trip){
        if (Objects.equals(startCity, destinationCity)){
            throw new ConflictException("La ciudad origen y destino no pueden ser iguales");
        }

        TripStop stopStartCity = tripStopRepository.findByTripIdAndCityIdAndDeletedAtIsNull(trip.getId(), startCity)
                .orElseThrow(() -> new ConflictException("La ciudad origen ingresada no pertenece al viaje"));

        TripStop stopDestinationCity = tripStopRepository
                .findByTripIdAndCityIdAndDeletedAtIsNull(trip.getId(), destinationCity)
                .orElseThrow(() -> new ConflictException("La ciudad destino ingresada no pertenece al viaje"));

        if (stopStartCity.getStopOrder() > stopDestinationCity.getStopOrder()) {
            throw new ConflictException("El orden de las ciudades seleccionadas no es válido para este viaje.");
        }

        LocalDateTime newStart = stopStartCity.getEstimatedArrivalDateTime();
        LocalDateTime newEnd   = stopDestinationCity.getEstimatedArrivalDateTime();

        log.info("Validando que el usuario no tenga reservas en progreso o aceptadass para el horario que quiere reservar");
        if (reservationRepository.hasOverlappingReservation(user.getId(), newStart, newEnd)) {
            throw new ConflictException(
                "Ya tenés una reserva activa que se superpone con el horario de este viaje."
            );
        }

        log.info("Verificando si el usuario tiene el rol de chofer.");
        if(user.hasRole("ROLE_DRIVER")){
            log.info("Validando que el usuario no tenga viajes pendientes, cerrados o en progreso para la hora a la que quiere reservar");
            if (tripRepository.hasOverlappingTripAsDriver(user.getDriver().getId(), newStart, newEnd)) {
                throw new ConflictException(
                "Tenés un viaje propio activo que se superpone con el horario de esta reserva."
                );
            }
        }
        // Retornar un arreglo de TripStop con las ciudades de inicio y destino
        return new TripStop[] { stopStartCity, stopDestinationCity };
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
            case "DATE_ASC"  -> Sort.by(CREATED_AT).ascending();
            case "DATE_DESC" -> Sort.by(CREATED_AT).descending();
            case "TRIP_DATE_ASC" -> Sort.by("trip.startDateTime").ascending();
            case "TRIP_DATE_DESC" -> Sort.by("trip.startDateTime").descending();
            default -> Sort.by(CREATED_AT).descending();
        };

        return PageRequest.of(page, PAGE_SIZE, sort);
    }

    /**
     * Obtiene el usuario autenticado actualmente.
     * Si no hay un usuario autenticado, lanza una excepción.
     *
     * @return User
     * @throws ResourceNotFoundException si no se encuentra un usuario autenticado.
     */
    private User getAuthenticatedActiveUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
    }
}
