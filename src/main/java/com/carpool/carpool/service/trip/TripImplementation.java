package com.carpool.carpool.service.trip;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.carpool.carpool.dto.trip.TripRequestDTO;
import com.carpool.carpool.dto.trip.TripResponseDTO;
import com.carpool.carpool.dto.trip.TripSearchRequestDTO;
import com.carpool.carpool.dto.trip.TripSearchResponseDTO;
import com.carpool.carpool.dto.trip.tripStop.TripStopRequestDTO;
import com.carpool.carpool.enums.state.ScopeEnum;
import com.carpool.carpool.enums.trip.BaggageEnum;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.mappers.trip.TripMapper;
import com.carpool.carpool.model.driver.Driver;
import com.carpool.carpool.model.state.State;
import com.carpool.carpool.model.stateHistory.StateHistory;
import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.model.vehicle.Vehicle;
import com.carpool.carpool.repository.city.CityRepository;
import com.carpool.carpool.repository.driver.DriverRepository;
import com.carpool.carpool.repository.state.StateRepository;
import com.carpool.carpool.repository.stateHistory.StateHistoryRepository;
import com.carpool.carpool.repository.trip.TripRepository;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.repository.vehicle.VehicleRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.utils.ResponseUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripImplementation implements ITripService{

    private final VehicleRepository vehicleRepository;
    private final TripMapper tripMapper;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final StateRepository stateRepository;
    private final StateHistoryRepository stateHistoryRepository;
    private final CityRepository cityRepository;

    @Value("${app.trip.default-city-id}")
    private Long defaultCityId;
    
    @Override
    @Transactional
    public Response<Void> createTrip(TripRequestDTO tripRequestDTO) {

        Vehicle vehicle = vehicleRepository.findById(tripRequestDTO.getIdVehicle())
        .orElseThrow(() -> new ResourceNotFoundException("El vehiculo no existe."));

        State stateCreate = stateRepository.findByNameAndScope("CREATE", ScopeEnum.TRIP)
        .orElseThrow(()->new ResourceNotFoundException("No se encontro el estado para crear el viaje."));

        //Validaciones del viaje en general 
        tripValidations(vehicle,tripRequestDTO);

        //Validacion para comprobar que la fecha de inicio del viaje es igual o posterior a la actual + 30 minutos
        if(tripRequestDTO.getStartDateTime().isBefore(LocalDateTime.now().plusMinutes(30))){
            throw new ConflictException("La fecha y hora del viaje deben tener un intervalo superior a 30 minutos desde la hora actual.");
        }

        //Validacion para comprobar que no existe otro viaje programado para el mismo vehiculo en la misma fecha y hora
        if (hasATripPlanned(tripRequestDTO.getStartDateTime())) {
            throw new ConflictException("Ya tenés un viaje programado en la misma fecha y hora.");
        }

        //Validaciones para las paradas intermedias
        startDestinationValidation(tripRequestDTO.getTripStops());
        validateTripStopsOrder(tripRequestDTO.getTripStops());

        Trip newTrip =  tripMapper.convertTripRequestDTOToTrip(tripRequestDTO, vehicle);

        StateHistory stateHistory = StateHistory.builder()
            .state(stateCreate)
        .build();
        
        stateHistory.setTripState(newTrip);
        
        tripRepository.save(newTrip);
        stateHistoryRepository.save(stateHistory);
        return ResponseUtils.buildOKResponse(List.of("Viaje creado con éxito") , null);
    }

    @Override
    public Response<TripResponseDTO> getTripDetails(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El viaje no existe."));

        User user = trip.getVehicle().getDriver().getUser();

        String driverFullName = user.getName() +
                " " + user.getLastname();

        Double driverRating = trip.getVehicle().getDriver().getRating();
        
        TripResponseDTO tripResponseDTO = tripMapper.convertTripToTripResponseDTO(trip, driverFullName, driverRating);
        return ResponseUtils.buildOKResponse(List.of("Viaje encontrado con éxito"), tripResponseDTO);
    }

    @Override
    public Response<Void> checkTripAvailability(LocalDateTime startDateTime) {
        if(hasATripPlanned(startDateTime)){
           throw new ConflictException("Ya tenés un viaje programado en la misma fecha y hora.");
        }else{
            return ResponseUtils.buildOKResponse(List.of("El viaje es posible"), null);
        }
    }

    @Override
    public Response<List<TripSearchResponseDTO>> getInitialFeed(Long userCityId, int limit) {

        Long userId = getAuthenticatedUserId();

        String infoMessage = null;

        if (userCityId == null) {
            userCityId = this.defaultCityId;
            infoMessage = "No se proporcionó la ubicación actual del usuario, por lo que se cargaron los viajes que salen o pasan por " + cityRepository.findById(defaultCityId).get().getName();
        }
                
        List<Trip> trips = tripRepository.findTripsForInitialFeed(userCityId, userId);

        if (trips.size() > limit) {
            trips = trips.subList(0, limit);
        }

        List<TripSearchResponseDTO> responseDTOs = trips.stream()
            .map(tripMapper::converTripToTripSearchResponseDTO)
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
    public Response<List<TripSearchResponseDTO>> searchTrips(TripSearchRequestDTO request, int limit) {
        
        Long userId = getAuthenticatedUserId(); 
        
        if (request.getOriginCityId() == null || request.getDestinationCityId() == null) {
            throw new ConflictException("Los campos de origen y destino son obligatorios para la búsqueda de viajes.");
        }
        
        List<Trip> trips = tripRepository.findFilteredTrips(
            request.getOriginCityId(),
            request.getDestinationCityId(),
            request.getDepartureDate(),
            request.getMinPrice(),
            request.getMaxPrice(),
            request.getDriverRating(),
            userId,
            request.getOrderByDriverRating()
        );

        if (trips.size() > limit) {
            trips = trips.subList(0, limit);
        }
        
        List<TripSearchResponseDTO> responseDTOs = trips.stream()
            .map(tripMapper::converTripToTripSearchResponseDTO)
            .collect(Collectors.toList());

        String message;
        if (responseDTOs.isEmpty()) {
            message = "No se encontraron más viajes que coincidan con los criterios.";
        } else {
            message = String.format("Se cargaron %d viajes.", responseDTOs.size());
        }
 
        return ResponseUtils.buildOKResponse(List.of(message), responseDTOs);
    }

    /**
     * Validaciones del viaje en general. Comprobamos aspectos como:
     * - Que el vehiculo con el id ingresado sea del chofer que inicio el viaje (usuario en sesion)
     * - Que la cantidad de asientos ingresada no sea mayor a la cantidad de asientos que estaban definidos para ese vehiculo
     * - Que el equipaje ingresado este dentro de los posibles valores (ENUM)
     * @param vehicle El vehiculo obtenido con el ID ingresado en la request
     * @param tripRequestDTO la request para cargar el viaje
     */
    private void tripValidations(Vehicle vehicle, TripRequestDTO tripRequestDTO){

        //Validacion para comprobar si el vehiculo pertenece al conductor que esta tratando de crear el viaje
        Driver authenticatedDriver = getAuthenticatedDriver();

        if (!vehicle.getDriver().getId().equals(authenticatedDriver.getId())) {
            throw new ConflictException("El vehículo no pertenece al conductor autenticado.");
        }

        //Validaciones para el equipaje y la cantidad de asientos
        if(tripRequestDTO.getAvailableSeat() > vehicle.getAvailableSeats()){
            throw new ConflictException("La cantidad de asientos no corresponde con el vehiculo registrado.");
        }

        if(!BaggageEnum.contains(tripRequestDTO.getAvailableBaggage())){
            throw new ConflictException("El tipo de equipaje es inválido.");
        }
    }

    /**
     * Realizamos diferentes validaciones para comprobar:
     * -Que el origen y el destino del viaje no son la misma ciudad 
     * -Que hay un solo origen y un solo destino en toda la lista de paradas
     * -Que cada ciudad esta solo una vez en la lista de paradas
     * @param tripStops la lista de paradas de un viaje
     */
    private void startDestinationValidation(List<TripStopRequestDTO> tripStops){

        //Validacion para comprobar que la ciudad de origen y la de destino no son la misma
        Long idStartCity = tripStops.stream()
            .filter(TripStopRequestDTO::isStart)
            .map(TripStopRequestDTO::getCityId)
            .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("No se definio la ciudad de origen."));

        Long idDestinationCity = tripStops.stream()
            .filter(ts->ts.isDestination())
            .map(TripStopRequestDTO::getCityId)
            .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("No se definio la ciudad de destino."));  

        if(idStartCity == idDestinationCity) throw new ConflictException("La ciudad de origen y la ciudad de destino no puedne ser la misma");

        //Validacion para comprobar que hay un solo origen y un solo destino
        long starts = tripStops.stream().filter(TripStopRequestDTO::isStart).count();
        long destinations = tripStops.stream().filter(TripStopRequestDTO::isDestination).count();
        if (starts != 1 || destinations != 1) throw new ConflictException("Debe haber un solo origen y un solo destino en la lista de paradas.");
    
        //Validacion para comprbar que cada ciudad esta solo una vez en la lista de paradas
        boolean allCitiesUnique = tripStops.stream()
        .map(TripStopRequestDTO::getCityId)
        .allMatch(new HashSet<>()::add);

        if (!allCitiesUnique) throw new ConflictException("Cada ciudad puede estar solo en una parada. Si va a hacer mas paradas en la ciudad puede indicarlo en el campo de observaciones.");
    }
    
    /**
     * Realizamos una validacion para comporbar que el orden de las paradas no se repite
     * @param tripStops la lista de paradas del viaje
     */
    private void validateTripStopsOrder(List<TripStopRequestDTO> tripStops){
        //Validacion para controlar que los numeros de orden no se respitan en la lista de paradas
        boolean allOrderUnique = tripStops.stream()
        .map(TripStopRequestDTO::getOrder)
        .allMatch(new HashSet<>()::add);

        if (!allOrderUnique) throw new ConflictException("El orden en las paradas no se puede repetir."); 
    }

    /**
     * Obtiene el chofer autenticado en el contexto de seguridad.
     * @return El chofer autenticado en el contexto de seguridad.
     */
    private Driver getAuthenticatedDriver() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new ConflictException("Usuario autenticado no encontrado."));

        return driverRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ConflictException("No se encontró el perfil de chofer para el usuario autenticado."));
    }

    /**
     * Verifica si un chofer tiene un viaje planificado en una fecha y hora determinadas.
     * @param driverId El ID del chofer.
     * @param startDateTime La fecha y hora a partir de la cual verificar.
     * @return true si el chofer tiene un viaje planificado después de la fecha y hora dadas, false en caso contrario.
     */
    private boolean hasATripPlanned (LocalDateTime startDateTime){
        Driver driver = getAuthenticatedDriver();

        return tripRepository.existsByVehicleDriverIdAndStartTripDateTime(driver.getId(), startDateTime);
    }

    /**
     * Obtiene el ID del usuario autenticado en el contexto de seguridad. Sirve para excluir al usuario de los resultados en las busquedas de viajes.
     * @return El ID del usuario autenticado en el contexto de seguridad.
     */
    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsernameAndDeletedAtIsNull(username)
            .orElseThrow(() -> new ConflictException("Usuario autenticado no encontrado."))
            .getId();
    }
}
