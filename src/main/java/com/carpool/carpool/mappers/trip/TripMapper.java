package com.carpool.carpool.mappers.trip;

import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.trip.TripRequestDTO;
import com.carpool.carpool.dto.trip.TripResponseDTO;
import com.carpool.carpool.enums.trip.BaggageEnum;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.model.trip.tripStop.TripStop;
import com.carpool.carpool.model.vehicle.Vehicle;
import com.carpool.carpool.repository.city.CityRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TripMapper {   
    private CityRepository cityRepository;
    
    public Trip convertTripRequestDTOToTrip(TripRequestDTO tripRequestDTO, Vehicle vehicle){ 
        Trip trip = Trip.builder()
            .startTripDateTime(tripRequestDTO.getStartDateTime())
            .availableSeat(tripRequestDTO.getAvailableSeat())
            .availableBaggage(BaggageEnum.valueOf(tripRequestDTO.getAvailableBaggage()))
            .vehicle(vehicle)
            .seatPrice(tripRequestDTO.getSeatPrice())
        .build();
        
        tripRequestDTO.getTripStops().forEach(tripStopDto ->{
            TripStop tripStop = TripStop.builder()
                .city(cityRepository.findById(tripStopDto.getCityId()).orElseThrow(()-> new ResourceNotFoundException("La ciudad con el ID " + tripStopDto.getCityId() + "no existe.")))
                .isStart(tripStopDto.getIsStart())
                .isDestination(tripStopDto.getIsDestination())
                .observation(tripStopDto.getObservation())
                .stopOrder(tripStopDto.getOrder())
            .build();
            trip.getTripStops().add(tripStop);            
        });

        return trip;

    }

    public TripResponseDTO convertTripToTripResponseDTO(Trip trip, String driverName) {
        return TripResponseDTO.builder()
            .id(trip.getId())
            .driverName(driverName)
            .startDateTime(trip.getStartTripDateTime())
            .availableSeat(trip.getAvailableSeat())
            .availableBaggage(trip.getAvailableBaggage().toString())
            .seatPrice(trip.getSeatPrice())
            .build();
    }
}
