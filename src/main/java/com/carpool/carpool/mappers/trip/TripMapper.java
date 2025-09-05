package com.carpool.carpool.mappers.trip;

import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.trip.TripRequestDTO;
import com.carpool.carpool.dto.trip.TripResponseDTO;
import com.carpool.carpool.enums.trip.BaggageEnum;
import com.carpool.carpool.model.province.town.Town;
import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.model.vehicle.Vehicle;

@Component
public class TripMapper {
    
    public Trip convertTripRequestDTOToTrip(TripRequestDTO tripRequestDTO, Town originTownId, Town destinationTownId, Vehicle vehicle){ {
        return Trip.builder()
            .startTripDateTime(tripRequestDTO.getStartDateTime())
            .originTown(originTownId)
            .destinationTown(destinationTownId)
            .intermediateTown(tripRequestDTO.getIntermediateTown())
            .availableSeat(tripRequestDTO.getAvailableSeat())
            .availableBaggage(BaggageEnum.valueOf(tripRequestDTO.getAvailableBaggage()))
            .vehicle(vehicle)
            .seatPrice(tripRequestDTO.getSeatPrice())
            .build();
        }
    }


    public TripResponseDTO convertTripToTripResponseDTO(Trip trip,  String driverName, String originTownName, String destinationTownName) {
        return TripResponseDTO.builder()
            .id(trip.getId())
            .driverName(driverName)
            .originTown(originTownName)
            .destinationTown(destinationTownName)
            .intermediateTown(trip.getIntermediateTown())
            .startDateTime(trip.getStartTripDateTime())
            .availableSeat(trip.getAvailableSeat())
            .availableBaggage(trip.getAvailableBaggage().toString())
            .seatPrice(trip.getSeatPrice())
            .build();
    }
}
