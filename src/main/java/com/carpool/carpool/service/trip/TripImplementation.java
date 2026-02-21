package com.carpool.carpool.service.trip;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.carpool.carpool.dto.trip.*;
import com.carpool.carpool.enums.reservation.ReservationStateEnum;
import com.carpool.carpool.enums.trip.TripStateEnum;
import com.carpool.carpool.service.state.TripStateHistoryFinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.carpool.carpool.dto.trip.CurrentTripResponseDTO;
import com.carpool.carpool.dto.trip.TripArriveRequestDTO;
import com.carpool.carpool.dto.trip.TripDriverDTO;
import com.carpool.carpool.dto.trip.TripDriverResponseDTO;
import com.carpool.carpool.dto.trip.TripHistoryUserDTO;
import com.carpool.carpool.dto.trip.TripHistoryUserResponseDTO;
import com.carpool.carpool.dto.trip.TripPriceCalculationResponseDTO;
import com.carpool.carpool.dto.trip.TripRequestDTO;
import com.carpool.carpool.dto.trip.TripResponseDTO;
import com.carpool.carpool.dto.trip.TripSearchRequestDTO;
import com.carpool.carpool.dto.trip.TripSearchResponseDTO;
import com.carpool.carpool.dto.trip.tripStop.TripStopRequestDTO;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.enums.state.ScopeEnum;
import com.carpool.carpool.enums.trip.BaggageEnum;
import com.carpool.carpool.exception.BadRequestException;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.mappers.trip.TripMapper;
import com.carpool.carpool.model.driver.Driver;
import com.carpool.carpool.model.province.city.City;
import com.carpool.carpool.model.reservation.Reservation;
import com.carpool.carpool.model.state.State;
import com.carpool.carpool.model.stateHistory.StateHistory;
import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.model.trip.tripStop.TripStop;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.model.vehicle.Vehicle;
import com.carpool.carpool.repository.city.CityRepository;
import com.carpool.carpool.repository.driver.DriverRepository;
import com.carpool.carpool.repository.reservation.ReservationRepository;
import com.carpool.carpool.repository.state.StateRepository;
import com.carpool.carpool.repository.stateHistory.StateHistoryRepository;
import com.carpool.carpool.repository.trip.TripRepository;
import com.carpool.carpool.repository.trip.stop.TripStopRepository;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.repository.vehicle.VehicleRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.parameters.IParametersService;
import com.carpool.carpool.service.reservation.IReservationService;
import com.carpool.carpool.service.state.StateTransitionService;
import com.carpool.carpool.utils.ResponseUtils;
import com.carpool.carpool.utils.TripCostUtils;
import com.carpool.carpool.service.notification.INotificationService;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripImplementation implements ITripService {

    private final VehicleRepository vehicleRepository;
    private final TripMapper tripMapper;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final StateRepository stateRepository;
    private final StateHistoryRepository stateHistoryRepository;
    private final CityRepository cityRepository;
    private final ReservationRepository reservationRepository;
    private final TripStopRepository tripStopRepository;
    private final IParametersService settingService;
    private final IReservationService reservationService;
    private final INotificationService notificationService;
    private final StateTransitionService stateTransitionService;
    private final TripStateHistoryFinder tripStateHistoryFinder;

    private static final String STATE_ACCEPTED = "ACCEPTED";

    @Override
    @Transactional
    public Response<Void> createTrip(TripRequestDTO tripRequestDTO) {

        Driver authenticatedDriver = getAuthenticatedDriver();

        Vehicle vehicle = vehicleRepository.findById(tripRequestDTO.getIdVehicle())
                .orElseThrow(() -> new ResourceNotFoundException("El vehiculo no existe."));

        State stateCreate = stateRepository.findByNameAndScope("CREATED", ScopeEnum.TRIP)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el estado para crear el viaje."));

        // Validaciones del viaje en general
        tripValidations(vehicle, tripRequestDTO);

        // Validaciones para las paradas intermedias
        startDestinationValidation(tripRequestDTO.getTripStops());
        validateTripStopsOrder(tripRequestDTO.getTripStops());

        Trip newTrip = tripMapper.convertTripRequestDTOToTrip(tripRequestDTO, vehicle);

        // Obtenemos la fecha y hora estimada de llegada de la parada marcada como
        // destino.
        LocalDateTime newEnd = newTrip.getTripStops().stream()
                .filter(TripStop::isDestination)
                .map(TripStop::getEstimatedArrivalDateTime)
                .findFirst()
                .orElseThrow(() -> new ConflictException("No se pudo calcular la fecha de llegada."));

        // Verfica que el chofer no tenga un viaje en progreso para publicar un viaje
        if (tripRepository.hasTripInProgress(authenticatedDriver.getId())) {
            throw new ConflictException("No podés publicar un nuevo viaje mientras tenés uno en curso.");
        }
        // Se verifica si el nuevo viaje interfiere con otros viajes del chofer.
        if (tripRepository.hasOverlappingSchedule(authenticatedDriver.getId(), tripRequestDTO.getStartDateTime(),
                newEnd)) {
            throw new ConflictException(
                    "El horario para iniciar el viaje se superpone con otro viaje activo. Por favor, elige otro horario.");
        }

        // Calculo para obtener el extra que se debe de pagar
        double totalCommissionPerSeat = (double) tripRequestDTO.getSeatPrice()
                * (settingService.getDiscountPercentage() / 100.0);

        double requestedPrice = newTrip.getSeatPrice();

        newTrip.setPublishedSeatPrice(requestedPrice + totalCommissionPerSeat);
        newTrip.setDriverPriceDiscount(totalCommissionPerSeat);

        StateHistory stateHistory = StateHistory.builder()
                .state(stateCreate)
                .build();

        stateHistory.setTrip(newTrip);
        tripRepository.save(newTrip);

        stateHistoryRepository.save(stateHistory);
        return ResponseUtils.buildOKResponse(List.of("Viaje creado con éxito"), null);
    }

    @Override
    public Response<TripResponseDTO> getTripDetails(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El viaje no existe."));

        TripResponseDTO tripResponseDTO = tripMapper.convertTripToTripResponseDTO(trip);
        return ResponseUtils.buildOKResponse(List.of("Viaje encontrado con éxito"), tripResponseDTO);
    }

    @Override
    public Response<TripDriverResponseDTO> getTrips(List<String> tripState) {
        Driver driver = getAuthenticatedDriver();

        List<Trip> trips = tripRepository.findTripsByDriverIdWithCurrentStateTrip(driver.getId(), tripState);
        if (trips.isEmpty()) {
            return ResponseUtils.buildOKResponse(List.of("No existen viajes publicados por el chofer"), null);
        }

        List<TripDriverDTO> listTripDriver = tripMapper.convertTripToTripDriverResponseDTO(trips);
        TripDriverResponseDTO response = new TripDriverResponseDTO();
        response.setTrips(listTripDriver);

        return ResponseUtils.buildOKResponse(List.of("Listado de viajes obtenido con éxito"), response);
    }

    @Override
    public Response<Void> checkTripAvailability(LocalDateTime startDateTime) {

        Driver driver = getAuthenticatedDriver();

        if (tripRepository.isTimeSlotOccupied(driver.getId(), startDateTime)) {
            throw new ConflictException("Ese horario coincide con un viaje que ya tenés en curso.");
        }

        return ResponseUtils.buildOKResponse(List.of("El horario de inicio está disponible"), null);
    }

    @Override
    public Response<List<TripSearchResponseDTO>> getInitialFeed(Long userCityId, int limit) {

        Long userId = getAuthenticatedUserId();

        String infoMessage = null;

        if (userCityId == null) {
            userCityId = settingService.getDefaultCityId();
            infoMessage = "No se proporcionó la ubicación actual del usuario, por lo que se cargaron los viajes que salen o pasan por "
                    + cityRepository.findById(userCityId).get().getName();
        }

        List<Trip> trips = tripRepository.findTripsForInitialFeed(userCityId, userId, LocalDateTime.now());

        if (trips.size() > limit) {
            trips = trips.subList(0, limit);
        }
        City originCity = cityRepository.findById(userCityId)
                .orElseThrow(() -> new ConflictException("No se pudo encontrar la ciudad de origen del usuario."));

        List<TripSearchResponseDTO> responseDTOs = trips.stream()
                .map(trip -> tripMapper.converTripToTripSearchResponseDTO(trip,
                        TripCostUtils.calculateTripTotal(originCity, null, trip)))
                .collect(Collectors.toList());

        List<String> messages = new ArrayList<>();

        if (responseDTOs.isEmpty()) {
            messages.add("No se encontraron viajes que coincidan con los criterios.");
        } else {
            if (infoMessage != null) {
                messages.add(infoMessage);
            }
            messages.add(String.format("Se cargaron %d viajes.", responseDTOs.size()));
        }

        return ResponseUtils.buildOKResponse(messages, responseDTOs);

    }

	@Override
	public Response<TripHistoryUserResponseDTO> getHistoryTripUser(List<String> namesStateTrip, int skip) {
		
		Long userId = getAuthenticatedUserId();
		log.info("Iniciando busqueda de historial de viajes para usuario con id: {}", userId);
		
		 List<String> existingStates = stateRepository.findExistingStateNames(ScopeEnum.TRIP, namesStateTrip);
		 
		 // Se valida la existencia de los estados
		 if(existingStates.size() != namesStateTrip.size()) {
			List<String> invalidStates = namesStateTrip.stream().filter(state -> !existingStates.contains(state)).toList();

			throw new BadRequestException("Estados inválidos: " + String.join(", ", invalidStates));
		 }
		
		// Se buscan los viajes  del usuario con el estado correspondiente
		Page<Trip> tripsPage = tripRepository.findTripsByUserAndCurrentStates(userId, namesStateTrip, getPageable(skip));
		log.info("Cantidad de historial de viajes obtenidos: [{}]", tripsPage.getTotalElements());
		List<Trip> trips = tripsPage.getContent();
		
	    if(trips.isEmpty()){return ResponseUtils.buildOKResponse(List.of("El pasajero no cuenta con viajes"), null);}
		
	    // Se obtienen los id de los viajes
	    List<Long> tripIds = trips.stream().map(Trip::getId).toList();
	    
	    // Se obtienen los estados actuales de cada viaje
	    List<StateHistory> currentStates = stateHistoryRepository.findCurrentStatesByTripIds(tripIds);
	    
	    // Se obtienen las reservas del usuario en cada viaje
	    List<Reservation> reservations = reservationRepository.findByTripIdInAndUserId(tripIds, userId);
	    
	    Map<Long, StateHistory> stateMap =
	            currentStates.stream()
	                    .collect(Collectors.toMap(
	                            sh -> sh.getTrip().getId(),
	                            Function.identity()
	                    ));
	    
	    Map<Long, Reservation> reservationMap =
	            reservations.stream()
	                    .collect(Collectors.toMap(
	                            r -> r.getTrip().getId(),
	                            Function.identity()
	                    ));
	    
	    log.info("Iniciando mappeo de response a DTO");
	    List<TripHistoryUserDTO> tripDtos =
	            trips.stream()
	                    .map(trip -> tripMapper.convertTripToHistoryDTO(
	                            trip,
	                            reservationMap.get(trip.getId()),
	                            stateMap.get(trip.getId())
	                    ))
	                    .toList();
	    log.info("Operacion completada con exito");
	    
	    TripHistoryUserResponseDTO response =
	            TripHistoryUserResponseDTO.builder()
	                    .trips(tripDtos)
	                    .build();
	    
	    return ResponseUtils.buildOKResponse(
	    		List.of("Viajes obtenidos correctamente"),
	    		response
	    );
	}
    
    @Override
    public Response<Boolean> isTripCreator(Long tripId) {

        Long authenticatedUserId = getAuthenticatedUserId();
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("El viaje no existe."));

        boolean isCreator = trip.getVehicle().getDriver().getUser().getId().equals(authenticatedUserId);
        return ResponseUtils.buildOKResponse(
                List.of("Verificación realizada con éxito"),
                isCreator);
    }

    @Override
    public Response<List<TripSearchResponseDTO>> searchTrips(TripSearchRequestDTO request, int limit) {

        Long userId = getAuthenticatedUserId();

        if (request.getOriginCityId() == null || request.getDestinationCityId() == null) {
            throw new ConflictException("Los campos de origen y destino son obligatorios para la búsqueda de viajes.");
        }

        City originCity = cityRepository.findById(request.getOriginCityId())
                .orElseThrow(() -> new ConflictException("No se pudo encontrar la ciudad de origen de la busqueda."));

        City destinationCity = cityRepository.findById(request.getDestinationCityId())
                .orElseThrow(() -> new ConflictException("No se pudo encontrar la ciudad de destino de la busqueda."));

        List<Trip> trips = tripRepository.findFilteredTrips(
                request.getOriginCityId(),
                request.getDestinationCityId(),
                request.getDepartureDate(),
                request.getMinPrice(),
                request.getMaxPrice(),
                userId,
                request.getOrderByDriverRating(),
                LocalDateTime.now());

        if (trips.size() > limit) {
            trips = trips.subList(0, limit);
        }

        List<TripSearchResponseDTO> responseDTOs = trips.stream()
                .map(trip -> tripMapper.converTripToTripSearchResponseDTO(trip,
                        TripCostUtils.calculateTripTotal(originCity, destinationCity, trip)))
                .collect(Collectors.toList());

        String message;
        if (responseDTOs.isEmpty()) {
            message = "No se encontraron más viajes que coincidan con los criterios.";
        } else {
            message = String.format("Se cargaron %d viajes.", responseDTOs.size());
        }

        return ResponseUtils.buildOKResponse(List.of(message), responseDTOs);
    }

    @Override
    public Response<TripPriceCalculationResponseDTO> calculatePublishSeatPrice(Double seatPrice,
            Integer availableCurrentSeats) {

        if (availableCurrentSeats == null || availableCurrentSeats <= 0) {
            throw new ConflictException("La cantidad de asientos disponibles debe ser un número positivo.");
        }

        if (seatPrice == null || seatPrice <= 0) {
            throw new ConflictException("El precio base del asiento debe ser un valor positivo.");
        }
        double totalCommissionPerSeat = seatPrice * (settingService.getDiscountPercentage() / 100.0);

        TripPriceCalculationResponseDTO calculation = tripMapper.convertTriptoTripPriceCalculationResponseDTO(seatPrice,
                totalCommissionPerSeat);

        return ResponseUtils.buildOKResponse(List.of("Cálculo de precios realizado con éxito"), calculation);
    }

    @Override
    @Transactional
    public Response<Void> startTrip(Long tripId) {
        Trip trip = tripRepository.findTripWithAllDetails(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Viaje no encontrado."));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduledStart = trip.getStartTripDateTime();

        if (now.isBefore(scheduledStart.minusMinutes(30))) {
            throw new ConflictException("Todavía es muy temprano. Podés iniciar el viaje hasta 30 minutos antes de las "
                    + scheduledStart.toLocalTime());
        }

        if (now.isAfter(scheduledStart.plusMinutes(15))) {
            notifyPassengers(trip, NotificationEventEnum.TRIP_CANCELLED_BY_SYSTEM);
            stateTransitionService.transition(trip, ScopeEnum.TRIP, TripStateEnum.CLOSED.name(), TripStateEnum.CANCELLED.name());
            cancelAllReservations(trip);
            return ResponseUtils.buildErrorResponse(List.of("El tiempo límite para iniciar el viaje ha expirado (máximo 15 min de demora). El viaje ha sido cancelado automáticamente."));
        }

        TripStop startStop = trip.getTripStops().stream()
            .filter(ts -> ts.getStopOrder() == 1)
            .findFirst()
            .orElseThrow(() -> new ConflictException("No se encontró la parada inicial del viaje."));

        startStop.setArrivalDateTime(now);
        notifyPassengers(trip, NotificationEventEnum.TRIP_STARTED);

        stateTransitionService.transition(trip, ScopeEnum.TRIP, TripStateEnum.CLOSED.name(),TripStateEnum.IN_PROGRESS.name());

        this.startTripReservation(trip);
        return ResponseUtils.buildOKResponse(List.of("¡Viaje iniciado! Que tengas un buen recorrido."), null);
    }

    @Override
    @Transactional
    public Response<Void> cancelTrip(TripCancellRequestDTO tripCancellRequestDTO) {
        log.info("Iniciando cancelación de viaje. tripId={}", tripCancellRequestDTO.getTripId());
        Trip trip = tripRepository.findTripWithAllDetails(tripCancellRequestDTO.getTripId())
                .orElseThrow(() -> {
                    log.error("Viaje no encontrado. tripId={}", tripCancellRequestDTO.getTripId());
                    return new ResourceNotFoundException("Viaje no encontrado.");
                });

        log.info("Buscando reservas aceptadas");
        List<Reservation> acceptedReservations = reservationRepository.findByTripIdAndStateName(trip.getId(),
                STATE_ACCEPTED);

        log.info("Buscando reservas pendientes");
        List<Reservation> pendingReservations = reservationRepository.findByTripIdAndStateName(trip.getId(), ReservationStateEnum.PENDING.name());

        // Validar que si hay reservas activas, debe haber una razón
        if (!acceptedReservations.isEmpty() || !pendingReservations.isEmpty())  {
            if (tripCancellRequestDTO.getReason() == null || tripCancellRequestDTO.getReason().isBlank()) {
                throw new ConflictException("Este viaje cuenta con reservas activas, por lo que tenés que justificar el motivo de su cancelación.");
            }
            trip.setCancellationReason(tripCancellRequestDTO.getReason());
        } else {
            // Si no hay reservas, la razón es opcional pero si se proporciona, guardarla
            if (tripCancellRequestDTO.getReason() != null && !tripCancellRequestDTO.getReason().isBlank()) {
                trip.setCancellationReason(tripCancellRequestDTO.getReason());
            }
        }

        List<Reservation> reservationsToCancel = new ArrayList<>();
        reservationsToCancel.addAll(acceptedReservations);
        reservationsToCancel.addAll(pendingReservations);

        log.info("Iniciando cambio de estado");
        stateTransitionService.transition(trip, ScopeEnum.TRIP, List.of(TripStateEnum.CREATED.name(), TripStateEnum.CLOSED.name()), TripStateEnum.CANCELLED.name());

        for (Reservation res : reservationsToCancel) {

            log.info("Cancelando reserva {} del viaje {}",
                    res.getId(), trip.getId());

            reservationService.cancelReservation(res.getId());

            this.notificationService.send(
                    res.getUser(),
                    NotificationEventEnum.TRIP_CANCELLED,
                    trip);
        }

        return ResponseUtils.buildOKResponse(List.of("Viaje cancelado con éxito."), null);
    }

    @Override
    public Response<CurrentTripResponseDTO> getCurrentTrip() {
        Driver driver = getAuthenticatedDriver();

        Optional<Trip> currentTripOpt =
            tripRepository.findCurrentTripByDriver(driver.getId());

        if (currentTripOpt.isEmpty()) {
            return ResponseUtils.buildOKResponse(
                List.of("El chofer no tiene un viaje en curso"),
                null
            );
        }

        return ResponseUtils.buildOKResponse(
            List.of("Viaje en curso recuperado con éxito"),
            tripMapper.covertTripToCurrentTripResponseDTO(currentTripOpt.get())
        );
    }


    @Override
    public Response<Void> arriveTripStop(TripArriveRequestDTO tripArriveRequestDTO){
        Driver driver = getAuthenticatedDriver();

        Trip currentTrip = tripRepository.findCurrentTripByDriver(driver.getId())
            .orElseThrow(() -> new EntityNotFoundException("El chofer no tiene un viaje en curso en este momento."));


        TripStop stop = currentTrip.getTripStops().stream()
            .filter(ts -> ts.getId() == tripArriveRequestDTO.getIdTripStop())
            .findFirst()
            .orElseThrow(() ->
                new EntityNotFoundException("Parada no encontrada en el viaje")
        );

        validateStopOrderToClose(currentTrip, stop);

        List<Reservation> reservations = reservationRepository.findReservationsByTripAndDestinationAndState(currentTrip.getId(), stop.getId(), "IN_PROGRESS");

        if(reservations != null && !reservations.isEmpty()){
            reservations.forEach(reservationService::finishTripReservation);
        }

        stop.setArrivalDateTime(LocalDateTime.now());
        tripStopRepository.save(stop);
        if(stop.isDestination()){
            return finishTrip(currentTrip);
        }else{
            return ResponseUtils.buildOKResponse(List.of("Llegada a parada registrada con éxito.") , null);
        }
    }

    private Response<Void> finishTrip(Trip trip){
        stateTransitionService.transition(trip, ScopeEnum.TRIP, "IN_PROGRESS", "FINISHED");
        return ResponseUtils.buildOKResponse(List.of("Viaje finalizado con éxito") , null);
    }

    /**
     * Metodo que se utiliza para validar el orden de una parda intermedia que se quiere cerrar para un viaje
     * Las paradas se deben cerrar en orden y no se puede cerrar si la anterior no tiene horario de llegada.
     * A su vez no es posible iniciar un vijae con este endpoint, solamente cerrar desde la segunda parada intermedia hasta
     * el destino
     */
    private void validateStopOrderToClose(Trip trip, TripStop stopToClose) {

        Optional<TripStop> lastClosedStop = trip.getTripStops().stream()
                .filter(ts -> ts.getArrivalDateTime() != null)
                .max(Comparator.comparingInt(TripStop::getStopOrder));

        if (lastClosedStop.isEmpty()) {

            throw new ConflictException(
                "No hay ninguna parada cerrada hasta el momento."
            );

        }

        int expectedOrder = lastClosedStop.get().getStopOrder() + 1;

        if (stopToClose.getStopOrder() != expectedOrder) {
            throw new ConflictException(
                "Orden inválido. Debe cerrarse la parada con orden " + expectedOrder
            );
        }
    }

    /**
     * Validaciones del viaje en general. Comprobamos aspectos como:
     * - Que el vehiculo con el id ingresado sea del chofer que inicio el viaje
     * (usuario en sesion)
     * - Que la cantidad de asientos ingresada no sea mayor a la cantidad de
     * asientos que estaban definidos para ese vehiculo
     * - Que el equipaje ingresado este dentro de los posibles valores (ENUM)
     *
     * @param vehicle        El vehiculo obtenido con el ID ingresado en la request
     * @param tripRequestDTO la request para cargar el viaje
     * @throws ConflictException si alguna de las validaciones falla
     */
    private void tripValidations(Vehicle vehicle, TripRequestDTO tripRequestDTO) {

        // Validacion para comprobar si el vehiculo pertenece al conductor que esta
        // tratando de crear el viaje
        Driver authenticatedDriver = getAuthenticatedDriver();

        if (!vehicle.getDriver().getId().equals(authenticatedDriver.getId())) {
            throw new ConflictException("El vehículo no pertenece al conductor autenticado.");
        }

        // Validaciones para el equipaje y la cantidad de asientos
        if (tripRequestDTO.getAvailableSeat() > vehicle.getAvailableSeats()) {
            throw new ConflictException("La cantidad de asientos no corresponde con el vehiculo registrado.");
        }

        if (!BaggageEnum.contains(tripRequestDTO.getAvailableBaggage())) {
            throw new ConflictException("El tipo de equipaje es inválido.");
        }
    }
    public User getAuthenticatedActiveUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
    }

    /**
     * Realizamos diferentes validaciones para comprobar:
     * -Que el origen y el destino del viaje no son la misma ciudad
     * -Que hay un solo origen y un solo destino en toda la lista de paradas
     * -Que cada ciudad esta solo una vez en la lista de paradas
     *
     * @param tripStops la lista de paradas de un viaje
     * @throws ConflictException si alguna de las validaciones falla
     */
    private void startDestinationValidation(List<TripStopRequestDTO> tripStops) {

        // Validacion para comprobar que la ciudad de origen y la de destino no son la
        // misma
        Long idStartCity = tripStops.stream()
                .filter(TripStopRequestDTO::isStart)
                .map(TripStopRequestDTO::getCityId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No se definio la ciudad de origen."));

        Long idDestinationCity = tripStops.stream()
                .filter(ts -> ts.isDestination())
                .map(TripStopRequestDTO::getCityId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No se definio la ciudad de destino."));

        if (idStartCity.equals(idDestinationCity))
            throw new ConflictException("La ciudad de origen y la ciudad de destino no puedne ser la misma");

        // Validacion para comprobar que hay un solo origen y un solo destino
        long starts = tripStops.stream().filter(TripStopRequestDTO::isStart).count();
        long destinations = tripStops.stream().filter(TripStopRequestDTO::isDestination).count();
        if (starts != 1 || destinations != 1)
            throw new ConflictException("Debe haber un solo origen y un solo destino en la lista de paradas.");

        // Validacion para comprbar que cada ciudad esta solo una vez en la lista de
        // paradas
        boolean allCitiesUnique = tripStops.stream()
                .map(TripStopRequestDTO::getCityId)
                .allMatch(new HashSet<>()::add);

        if (!allCitiesUnique)
            throw new ConflictException(
                    "Cada ciudad puede estar solo en una parada. Si va a hacer mas paradas en la ciudad puede indicarlo en el campo de observaciones.");
    }

    /**
     * Realizamos una validacion para comporbar que el orden de las paradas no se
     * repite
     *
     * @param tripStops la lista de paradas del viaje
     * @throws ConflictException si la validacion falla
     */
    private void validateTripStopsOrder(List<TripStopRequestDTO> tripStops) {
        // Validacion para controlar que los numeros de orden no se respitan en la lista
        // de paradas
        boolean allOrderUnique = tripStops.stream()
                .map(TripStopRequestDTO::getOrder)
                .allMatch(new HashSet<>()::add);

        if (!allOrderUnique)
            throw new ConflictException("El orden en las paradas no se puede repetir.");
    }

    /**
     * Obtiene el chofer autenticado en el contexto de seguridad.
     *
     * @return El chofer autenticado en el contexto de seguridad.
     * @throws ConflictException si el usuario autenticado no se encuentra o no
     *                           tiene un perfil de chofer asociado.
     */
    private Driver getAuthenticatedDriver() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new ConflictException("Usuario autenticado no encontrado."));

        return driverRepository.findByUserId(user.getId())
                .orElseThrow(
                        () -> new ConflictException("No se encontró el perfil de chofer para el usuario autenticado."));
    }

    /**
     * Obtiene el ID del usuario autenticado en el contexto de seguridad. Sirve para
     * excluir al usuario de los resultados en las busquedas de viajes.
     *
     * @return El ID del usuario autenticado en el contexto de seguridad.
     */
    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new ConflictException("Usuario autenticado no encontrado."))
                .getId();
    }

    /**
     * Cancela todas las reservas de un viaje.
     *
     * @param trip el viaje a cancelar
     */
    private void cancelAllReservations(Trip trip) {
        List<Reservation> reservations = reservationRepository.findByTripIdAndStateName(trip.getId(), STATE_ACCEPTED);

        for (Reservation reservation : reservations) {
            reservationService.cancelBySystem(reservation.getId());
        }
    }

    /**
     * Inicia todas las reservas de un viaje.
     *
     * @param trip el viaje a iniciar
     */
    private void startTripReservation(Trip trip) {
        List<Reservation> acceptedReservations = reservationRepository.findByTripIdAndStateName(trip.getId(),
                STATE_ACCEPTED);

        for (Reservation res : acceptedReservations) {
            reservationService.startTripReservation(res.getId());
        }
    }

    /**
     * Notifica a los pasajeros de un viaje.
     *
     * @param trip  el viaje a notificar
     * @param event el evento de notificacion
     */
    private void notifyPassengers(Trip trip, NotificationEventEnum event) {
        List<Reservation> acceptedReservations = reservationRepository.findByTripIdAndStateName(trip.getId(),
                STATE_ACCEPTED);

        for (Reservation res : acceptedReservations) {
            this.notificationService.send(
                    res.getUser(),
                    event,
                    res);
        }
    }
    
    /**
     * Permite crear un objeto {@link Pageable} para filtrar por paginado
     * @param skip	Pagina que se desea obtener
     * @return Objeto {@link Pageable}
     */
    private Pageable getPageable(int skip) {
        final int PAGE_SIZE = 10;
        int page = skip / PAGE_SIZE;

        return PageRequest.of(page, PAGE_SIZE);
      }
}
