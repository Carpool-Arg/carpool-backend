package com.carpool.carpool.service.trip;

import com.carpool.carpool.dto.trip.TripRequestDTO;
import com.carpool.carpool.dto.trip.TripResponseDTO;
import com.carpool.carpool.enums.trip.BaggageEnum;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.mappers.trip.TripMapper;
import com.carpool.carpool.model.driver.Driver;
import com.carpool.carpool.model.province.town.Town;
import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.model.vehicle.Vehicle;
import com.carpool.carpool.repository.driver.DriverRepository;
import com.carpool.carpool.repository.town.TownRepository;
import com.carpool.carpool.repository.trip.TripRepository;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.repository.vehicle.VehicleRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.utils.ResponseUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripImplementation implements ITripService{

    private final VehicleRepository vehicleRepository;
    private final TownRepository townRepository;
    private final TripMapper tripMapper;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;


    @Override
    @Transactional
    public Response<Void> createTrip(TripRequestDTO tripRequestDTO) {

        Driver authenticatedDriver = getAuthenticatedDriver();  

        Vehicle vehicle = vehicleRepository.findById(tripRequestDTO.getIdVehicle())
                .orElseThrow(() -> new ResourceNotFoundException("El vehiculo no existe.")); 
        Town originTown = townRepository.findById(tripRequestDTO.getOriginTownId())
                .orElseThrow(() -> new ResourceNotFoundException("La ciudad de origen no existe."));

        Town destinationTown = townRepository.findById(tripRequestDTO.getDestinationTownId())
                .orElseThrow(() -> new ResourceNotFoundException("La ciudad de destino no existe."));

       
        if (!vehicle.getDriver().getId().equals(authenticatedDriver.getId())) {
            throw new ConflictException("El vehículo no pertenece al conductor autenticado.");
        }
        if(tripRequestDTO.getOriginTownId().equals(tripRequestDTO.getDestinationTownId())){
            throw new ConflictException("La ciudad origen y destino no pueden ser las mismas.");
        }

        if(tripRequestDTO.getAvailableSeat() > vehicle.getAvailableSeats()){
            throw new ConflictException("La cantidad de asiento no corresponde con el vehiculo registrado.");
        }

        if(!BaggageEnum.contains(tripRequestDTO.getAvailableBaggage())){
            throw new ConflictException("El tipo de equipaje es inválido.");
        }

        Trip newTrip =  tripMapper.convertTripRequestDTOToTrip(tripRequestDTO, originTown, destinationTown, vehicle);
        tripRepository.save(newTrip);

        return ResponseUtils.buildOKResponse(List.of("Viaje creado con éxito") , null);
    }

    @Override
    public Response<TripResponseDTO> getTripDetails(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El viaje no existe."));

        User user = trip.getVehicle().getDriver().getUser();

        String driverFullName = user.getName() + 
                           " " + user.getLastname();
        
        
        TripResponseDTO tripResponseDTO = tripMapper.convertTripToTripResponseDTO(trip, driverFullName, trip.getOriginTown().getName(),trip.getDestinationTown().getName());
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
