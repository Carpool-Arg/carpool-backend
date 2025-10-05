package com.carpool.carpool.mappers.trip;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.trip.TripRequestDTO;
import com.carpool.carpool.dto.trip.TripResponseDTO;
import com.carpool.carpool.dto.trip.tripStop.TripStopRequestDTO;
import com.carpool.carpool.dto.trip.tripStop.TripStopResponseDTO;
import com.carpool.carpool.enums.trip.BaggageEnum;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.model.province.city.City;
import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.model.trip.tripStop.TripStop;
import com.carpool.carpool.model.vehicle.Vehicle;
import com.carpool.carpool.repository.city.CityRepository;
import com.carpool.carpool.utils.CoordsUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TripMapper {   
    private final CityRepository cityRepository;
    final double AVERAGE_SPEED_KMH = 80.0; // Velocidad promedio en km/h
    
    public Trip convertTripRequestDTOToTrip(TripRequestDTO tripRequestDTO, Vehicle vehicle){ 
        Trip trip = Trip.builder()
            .startTripDateTime(tripRequestDTO.getStartDateTime())
            .availableSeat(tripRequestDTO.getAvailableSeat())
            .availableBaggage(BaggageEnum.valueOf(tripRequestDTO.getAvailableBaggage()))
            .vehicle(vehicle)
            .seatPrice(tripRequestDTO.getSeatPrice())
            .tripStops(new ArrayList<>())
        .build();
        
        City previousCity = null;
        double totalDistanceAccumulated = 0.0;
        LocalDateTime currentArrivalTime = tripRequestDTO.getStartDateTime();

        // Recorremos las paradas en orden
        for (TripStopRequestDTO tripStopDto : tripRequestDTO.getTripStops()) {
            City currentCity = cityRepository.findById(tripStopDto.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "La ciudad con el ID " + tripStopDto.getCityId() + " no existe."
                ));

            double distanceFromPrevious = 0.0;
            if (previousCity != null) {
                distanceFromPrevious = CoordsUtils.calculateDistance(
                    previousCity.getLatitude(), previousCity.getLongitude(),
                    currentCity.getLatitude(), currentCity.getLongitude()
                );
            }

            totalDistanceAccumulated += distanceFromPrevious;

            double estimatedHours = totalDistanceAccumulated / AVERAGE_SPEED_KMH;
            currentArrivalTime = tripRequestDTO.getStartDateTime()
            .plusMinutes((long)(estimatedHours * 60));


            
            TripStop tripStop = TripStop.builder()
                .city(currentCity)
                .isStart(tripStopDto.isStart())
                .isDestination(tripStopDto.isDestination())
                .observation(tripStopDto.getObservation())
                .stopOrder(tripStopDto.getOrder())
                .trip(trip)
                .distanceFromPrevious(distanceFromPrevious)
                .estimatedArrivalDateTime(currentArrivalTime)
                .build();

            trip.getTripStops().add(tripStop);
            previousCity = currentCity;           
        };

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
