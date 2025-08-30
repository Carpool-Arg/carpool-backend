package com.carpool.carpool.service.trip;

import com.carpool.carpool.dto.trip.TripRequestDTO;
import com.carpool.carpool.enums.trip.BaggageEnum;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.model.vehicle.Vehicle;
import com.carpool.carpool.repository.vehicle.VehicleRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripImplementation implements ITripService{

    private final VehicleRepository vehicleRepository;

    @Override
    public Response<Void> createTrip(TripRequestDTO tripRequestDTO) {

        if(tripRequestDTO.getOriginTown().toLowerCase().equals(tripRequestDTO.getDestinationTown().toLowerCase())){
            throw new ConflictException("La ciudad origen y destino no pueden ser las mismas.");
        }

        Vehicle vehicle = vehicleRepository.findById(tripRequestDTO.getIdVehicle())
                .orElseThrow(() -> new ResourceNotFoundException("El vehiculo no existe."));

        if(tripRequestDTO.getAvailableSeat() > vehicle.getAvailableSeats()){
            throw new ConflictException("La cantidad de asiento no corresponde con el vehiculo registrado.");
        }

        if(!BaggageEnum.contains(tripRequestDTO.getAvailableBaggage())){
            throw new ConflictException("El tipo de equipaje es inválido.");
        }

        return ResponseUtils.buildOKResponse(List.of("Viaje credo con éxito") , null);
    }
}
