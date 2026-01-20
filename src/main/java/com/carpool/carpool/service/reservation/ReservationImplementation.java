package com.carpool.carpool.service.reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.carpool.carpool.dto.reservation.CreateReservationRequestDTO;
import com.carpool.carpool.dto.reservation.ReservationDTO;
import com.carpool.carpool.dto.reservation.ReservationResponseDTO;
import com.carpool.carpool.dto.reservation.ReservationUpdateRequestDTO;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.enums.state.ScopeEnum;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.ResourceNotFoundException;
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
import com.carpool.carpool.utils.ResponseUtils;
import com.carpool.carpool.utils.TripCostUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationImplementation implements IReservationService{
    private final TripRepository tripRepository;
    private final StateHistoryRepository stateHistoryRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final TripStopRepository  tripStopRepository;
    private final ReservationMapper reservationMapper;
    private final StateRepository stateRepository;
    private final INotificationService notificationService;
    private final IMediaService mediaService;
    private final CityRepository cityRepository;

    private static final String STATE_PENDING = "PENDING";

    @Override
    public Response<ReservationResponseDTO> getReservation(Long idTrip, Long idStartCity, Long idDestinationCity, Boolean baggage, String nameState, int page, int size) {
        User driver = getAuthenticatedActiveUser();

        Specification<Reservation> filter = ReservationSpecification.byFilter(idTrip, idStartCity, idDestinationCity, baggage, nameState, driver.getId());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Reservation> reservations = reservationRepository.findAll(filter, pageable);
        if(reservations.getTotalElements() == 0){
            return ResponseUtils.buildOKResponse(List.of("No existen reservas para el viaje correspondiente"), null);
        }

        Map<Long, StateHistory> stateHistoryMap = reservations.getContent().stream()
                .collect(Collectors.toMap(
                        Reservation::getId,
                        reservation -> stateHistoryRepository.findByReservationIdAndFinishDateTimeIsNull(reservation.getId())
                                .orElse(null)
                ));

        // Se procede a buscar las imagenes de perfil de cada pasajero que realizo la reserva al viaje
        Map<Long, String> urlImagesUsers = reservations.stream()
                .collect(Collectors.toMap(
                    reservation -> reservation.getUser().getId(),
                        reservation -> mediaService.getProfilePictureUrlByUserId(reservation.getUser().getId())
                ));

        List<ReservationDTO> listReservation = ReservationMapper.convertReservationToReservationDTO(reservations, urlImagesUsers, stateHistoryMap);
        ReservationResponseDTO responseReservation = new ReservationResponseDTO();
        responseReservation.setReservation(listReservation);

        return ResponseUtils.buildOKResponse(List.of("Reservas realizadas al viaje obtenido con éxito"), responseReservation);
    }

    @Override
    public Response<Void> createReservation(CreateReservationRequestDTO createReservationRequestDTO) {
        State statePending = stateRepository.findByNameAndScope("PENDING", ScopeEnum.RESERVATION)
                .orElseThrow(()->new ResourceNotFoundException("No se encontro el estado para crear la reserva."));

        //Validaciones de viaje
        Trip trip = tripRepository.findById(createReservationRequestDTO.getTrip())
                .orElseThrow(()->new ResourceNotFoundException("El viaje no existe"));

        tripValidations(trip);

        //Validaciones de usuario
        User userAuth = this.getAuthenticatedActiveUser();

        if (userAuth.getId().equals(trip.getVehicle().getDriver().getUser().getId())) {
            throw new ConflictException("Este viaje te pertenece, no podés realizar una reserva en él.");
        }

        Optional<Reservation> existingReservation = reservationRepository.findReservationByUserAndTrip(userAuth.getId(), trip.getId());
        if (existingReservation.isPresent()) {
            throw new ConflictException("Ya tenés una reserva asociada para este viaje, no se permiten múltiples solicitudes.");
        }

        // Validaciones de las ciudades
        TripStop[] tripStops =  cityValidations(createReservationRequestDTO.getStartCity(), createReservationRequestDTO.getDestinationCity(), trip);

        Reservation newReservation = reservationMapper.convertReservationRequestDTOToReservation(
                createReservationRequestDTO,
                userAuth,
                trip,
                tripStops[0],
                tripStops[1],
                TripCostUtils.calculateTripTotal(tripStops[0].getCity(), tripStops[1].getCity(), trip)
        );

        //Creacion de reserva
        StateHistory stateHistory = StateHistory.builder()
                .reservation(newReservation)
                .state(statePending)
                .build();

        reservationRepository.save(newReservation);
        stateHistoryRepository.save(stateHistory);

        this.notificationService.send(
                trip.getVehicle().getDriver().getUser(),
                NotificationEventEnum.RESERVATION_CREATED,
                newReservation
        );

        return ResponseUtils.buildOKResponse(List.of("Reserva registrada éxito, se encuentra pendiente a confirmación.") , null);
    }

    @Override
    public Response<Void> updateStateReservation(ReservationUpdateRequestDTO reservationUpdateRequestDTO) {

        Reservation reservation = reservationRepository.getReferenceById(reservationUpdateRequestDTO.getIdReservation());
        if(reservation == null){
            throw new ResourceNotFoundException("La reserva no existe");
        }

        StateHistory lastestStateReservation = stateHistoryRepository.findByReservationIdAndFinishDateTimeIsNull(reservation.getId()).orElseThrow(() -> new ConflictException("La reserva no tiene un estado actual."));
        if(!STATE_PENDING.equals(lastestStateReservation.getState().getName())){
            throw new ConflictException("No se puede realizar acciones a la reserva ya que la misma no esta pendiente.");
        }
        lastestStateReservation.setFinishDateTime(LocalDateTime.now());
        Trip trip = reservation.getTrip();

        State state = null;
        NotificationEventEnum notification = null;
        if(reservationUpdateRequestDTO.isReject()){
            state = stateRepository.findByNameAndScope("REJECTED", ScopeEnum.RESERVATION)
                    .orElseThrow(()->new ResourceNotFoundException("No se encontro el estado para cancelar la reserva."));

            notification = NotificationEventEnum.RESERVATION_REJECTED;
        }else{
            state = stateRepository.findByNameAndScope("ACCEPTED", ScopeEnum.RESERVATION)
                    .orElseThrow(()->new ResourceNotFoundException("No se encontro el estado para aceptar la reserva."));

            final int discountAvailableSeat = trip.getCurrentAvailableSeats() - 1;
            if(discountAvailableSeat < 0){
                throw new ConflictException("Se alcanzó el cupo disponible, no se puede aceptar la reserva.");
            }
            trip.setCurrentAvailableSeats(discountAvailableSeat);
            tripRepository.save(trip);
            notification = NotificationEventEnum.RESERVATION_ACCEPTED;
        }

        StateHistory stateHistory = StateHistory.builder()
                .reservation(reservation)
                .state(state)
                .build();

        reservationRepository.save(reservation);
        stateHistoryRepository.save(lastestStateReservation);
        stateHistoryRepository.save(stateHistory);

        this.notificationService.send(
                reservation.getUser(),
                notification,
                reservation
        );

        String message = String.format(
                "Reserva %s con éxito",
                reservationUpdateRequestDTO.isReject() ? "cancelada" : "aceptada");
        return ResponseUtils.buildOKResponse(List.of(message), null);
    }

    @Override
    public Response<Double> calculateTotal(Long idTrip, Long idStartCity, Long idDestinationCity){
        Trip trip = tripRepository.findById(idTrip)
            .orElseThrow(()->new ResourceNotFoundException("El viaje no existe."));

        City startCity = cityRepository.findById(idStartCity)
            .orElseThrow(()-> new ConflictException("No se pudo encontrar la ciudad de origen."));

        City destinationCity = cityRepository.findById(idDestinationCity)
            .orElseThrow(()-> new ConflictException("No se pudo encontrar la ciudad de destino."));

        return ResponseUtils.buildOKResponse(List.of("Total calculado con exito"), TripCostUtils.calculateTripTotal(startCity, destinationCity, trip));
    }

    /**
     * Validaciones relacionadas al viaje. Comprobamos lo siguiente:
     * - Que el viaje NO esté lleno
     * - que no esté en curso
     * - Que no esté cerrado
     * - Que no esté cancelado
     * - Que no esté finalizado
     * @param trip viaje que se quiere reservar
     */
    private void tripValidations(Trip trip){
        // 1. Validar que no esté lleno
        if (trip.getCurrentAvailableSeats() == 0){
            throw new ConflictException("Lo sentimos, no es posible reservar ya que el viaje está lleno.");
        }
        State currentState = null;
        // 2. obtener el estado actual (sin fecha fin)
        Optional<StateHistory> currentStateOptional = stateHistoryRepository.findByTripAndFinishDateTimeIsNullAndReservationIdIsNull(trip);

        if(!currentStateOptional.isPresent()){
            //3. Si no se encuentra un estado actual, obtener el ultimo estado con fecha fin
            StateHistory stateHistory = currentStateOptional
                    .orElseGet(() -> stateHistoryRepository
                            .findTopByTripAndFinishDateTimeIsNotNullOrderByFinishDateTimeDesc(trip)
                            .orElseThrow(() -> new ResourceNotFoundException("No se encontró un estado válido para el viaje")));

            currentState = stateHistory.getState();
        } else {
            currentState = currentStateOptional.get().getState();
        }

        // 4. validar estados
        if (!currentState.getName().equals("CREATED")){
            throw new ConflictException("No es posible reservar el viaje, debido a su estado actual.");
        }
    }

    /**
     * Validaciones relacionadas a las ciudades. Comprobamos lo siguiente:
     * - Que el viaje NO esté lleno
     * - La localidad origen y destino no pueden ser iguales. LISTO
     * - La localidad origen y destino deben existir en la tabla TripStop, en base al trip que se envia por la request
     * - Que la localidad origen y destino que se pasan, respeten el orden establecido en TripStop
     * @param startCity ciduad origen
     * @param destinationCity ciduad destino
     * @param trip viaje para el cual se solicita la reserva
     * @return TripStop[] Arreglo con los TripStops correspondientes a las ciudades de origen y destino válidas.
     * @throws ConflictException si las ciudades son iguales, si no existen en tripStop o no respetan el orden.
     */
    private TripStop[] cityValidations(Long startCity, Long destinationCity, Trip trip){
        if (Objects.equals(startCity, destinationCity)){
            throw new ConflictException("La ciudad origen y destino no pueden ser iguales");
        }

        TripStop stopStartCity = tripStopRepository.findByTripIdAndCityId(trip.getId(), startCity)
                .orElseThrow(()->new ConflictException("La ciudad origen ingresada no pertenece al viaje"));

        TripStop stopDestinationCity =  tripStopRepository.findByTripIdAndCityId(trip.getId(), destinationCity)
                .orElseThrow(()->new ConflictException("La ciudad destino ingresada no pertenece al viaje"));

        if (stopStartCity.getStopOrder() > stopDestinationCity.getStopOrder()){
            throw new ConflictException("El orden de las ciudades seleccionadas no es válido para este viaje.");
        }

        // Retornar un arreglo de TripStop con las ciudades de inicio y destino
        return new TripStop[] { stopStartCity, stopDestinationCity };
    }

    /**
     * Obtiene el usuario autenticado actualmente.
     * Si no hay un usuario autenticado, lanza una excepción.
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

