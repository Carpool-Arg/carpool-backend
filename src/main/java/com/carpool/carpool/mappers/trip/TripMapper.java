package com.carpool.carpool.mappers.trip;

import com.carpool.carpool.model.state.State;
import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.trip.TripRequestDTO;
import com.carpool.carpool.dto.trip.TripResponseDTO;
import com.carpool.carpool.enums.trip.BaggageEnum;
import com.carpool.carpool.model.province.city.City;
import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.model.vehicle.Vehicle;

@Component
public class TripMapper {
    
    public Trip convertTripRequestDTOToTrip(TripRequestDTO tripRequestDTO, City originCityId, City destinationCityId, Vehicle vehicle, State state){ {
        return Trip.builder()
            .startTripDateTime(tripRequestDTO.getStartDateTime())
            .originCity(originCityId)
            .destinationCity(destinationCityId)
            .intermediateCity(tripRequestDTO.getIntermediateCity())
            .availableSeat(tripRequestDTO.getAvailableSeat())
            .availableBaggage(BaggageEnum.valueOf(tripRequestDTO.getAvailableBaggage()))
            .state(state)
            .vehicle(vehicle)
            .seatPrice(tripRequestDTO.getSeatPrice())
            .build();
        }
    }


    public TripResponseDTO convertTripToTripResponseDTO(Trip trip, String driverName, String originCityName, String destinationCityName) {
        return TripResponseDTO.builder()
            .id(trip.getId())
            .driverName(driverName)
            .originCity(originCityName)
            .destinationCity(destinationCityName)
            .intermediateCity(trip.getIntermediateCity())
            .startDateTime(trip.getStartTripDateTime())
            .availableSeat(trip.getAvailableSeat())
            .availableBaggage(trip.getAvailableBaggage().toString())
            .seatPrice(trip.getSeatPrice())
            .build();
    }
}
