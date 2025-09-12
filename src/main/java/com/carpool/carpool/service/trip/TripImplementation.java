package com.carpool.carpool.service.trip;

import java.util.HashSet;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.carpool.carpool.dto.trip.TripRequestDTO;
import com.carpool.carpool.dto.trip.TripResponseDTO;
import com.carpool.carpool.dto.trip.tripStop.TripStopRequestDTO;
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
    
    @Override
    @Transactional
    public Response<Void> createTrip(TripRequestDTO tripRequestDTO) {

        Vehicle vehicle = vehicleRepository.findById(tripRequestDTO.getIdVehicle())
        .orElseThrow(() -> new ResourceNotFoundException("El vehiculo no existe."));

        State stateCreate = stateRepository.findByName("CREATE")
        .orElseThrow(()->new ResourceNotFoundException("No se encontro el estado para crear el viaje."));

        //Validaciones del viaje en general 
        tripValidations(vehicle,tripRequestDTO);

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

    @Override
    public Response<TripResponseDTO> getTripDetails(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El viaje no existe."));

        User user = trip.getVehicle().getDriver().getUser();

        String driverFullName = user.getName() + 
                           " " + user.getLastname();
        
        
        TripResponseDTO tripResponseDTO = tripMapper.convertTripToTripResponseDTO(trip, driverFullName);
        return ResponseUtils.buildOKResponse(List.of("Viaje encontrado con éxito"), tripResponseDTO);
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
}
