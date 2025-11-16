package com.carpool.carpool.dto.trip;

import com.carpool.carpool.dto.vehicle.VehicleResponseTripDTO;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TripDriverDTO {
    private Long id;
    private VehicleResponseTripDTO vehicle;
    private String startCity;
    private String destinationCity;
    private LocalDateTime startDateTime;
    private int availableSeat;
    private int currentAvailableSeats;
    private String availableBaggage;
    private double seatPrice;
    private LocalDateTime estimatedArrivalDateTime;
}
