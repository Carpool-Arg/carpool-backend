package com.carpool.carpool.dto.trip;

import java.time.LocalDateTime;

import com.carpool.carpool.dto.vehicle.VehicleResponseTripDTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripHistoryUserDTO {
    private Long tripId;
    private LocalDateTime startDateTime;
    private String driverName;
    private String driverProfileImage;
    private Double driverRating;
    private VehicleResponseTripDTO vehicle;
    private String startCity;
    private String destinationCity;
    private double seatPrice;
    private boolean reviewed;
    private String tripState;
}
