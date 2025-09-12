package com.carpool.carpool.mappers.trip;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.trip.TripRequestDTO;
import com.carpool.carpool.dto.trip.TripResponseDTO;
import com.carpool.carpool.dto.trip.tripStop.TripStopResponseDTO;
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
    private final CityRepository cityRepository;
    
    public Trip convertTripRequestDTOToTrip(TripRequestDTO tripRequestDTO, Vehicle vehicle){ 
        Trip trip = Trip.builder()
            .startTripDateTime(tripRequestDTO.getStartDateTime())
            .availableSeat(tripRequestDTO.getAvailableSeat())
            .availableBaggage(BaggageEnum.valueOf(tripRequestDTO.getAvailableBaggage()))
            .vehicle(vehicle)
            .seatPrice(tripRequestDTO.getSeatPrice())
            .tripStops(new ArrayList<>())
        .build();
        
        tripRequestDTO.getTripStops().forEach(tripStopDto ->{
            TripStop tripStop = TripStop.builder()
                .city(cityRepository.findById(tripStopDto.getCityId()).orElseThrow(()-> new ResourceNotFoundException("La ciudad con el ID " + tripStopDto.getCityId() + " no existe.")))
                .isStart(tripStopDto.isStart())
                .isDestination(tripStopDto.isDestination())
                .observation(tripStopDto.getObservation())
                .stopOrder(tripStopDto.getOrder())
                .trip(trip)
            .build();
            trip.getTripStops().add(tripStop);            
        });

        return trip;

    }

    public TripResponseDTO convertTripToTripResponseDTO(Trip trip, String driverName) {
       List<TripStopResponseDTO> tripStopDTOs = trip.getTripStops().stream()
        .map(tripStop -> TripStopResponseDTO.builder()
            .cityName(tripStop.getCity().getName())
            .observation(tripStop.getObservation())
            .build())
        .collect(Collectors.toList());

        return TripResponseDTO.builder()
            .id(trip.getId())
            .driverName(driverName)
            .tripStops(tripStopDTOs)
            .startDateTime(trip.getStartTripDateTime())
            .availableSeat(trip.getAvailableSeat())
            .availableBaggage(trip.getAvailableBaggage().toString())
            .seatPrice(trip.getSeatPrice())
            .build();
    }
}
