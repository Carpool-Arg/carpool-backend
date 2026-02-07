package com.carpool.carpool.dto.trip;

import java.time.LocalDateTime;
import java.util.List;

import com.carpool.carpool.dto.trip.tripStop.TripStopResponseDTO;
import com.carpool.carpool.dto.vehicle.VehicleResponseTripDTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripHistoryResponseDTO {
    private Long tripId;
    private LocalDateTime startDateTime;
    private String driverName;
    private String driverProfileImage;
    private Double driverRating;
    private VehicleResponseTripDTO vehicle;
    private List<TripStopResponseDTO> tripStops;
}
