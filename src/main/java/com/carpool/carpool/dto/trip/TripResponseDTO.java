package com.carpool.carpool.dto.trip;

import java.time.LocalDateTime;
import java.util.List;

import com.carpool.carpool.dto.driver.DriverSearchResponseDTO;
import com.carpool.carpool.dto.trip.tripStop.TripStopResponseDTO;
import com.carpool.carpool.dto.vehicle.VehicleResponseTripDTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripResponseDTO {
    private Long id;
    private DriverSearchResponseDTO driverInfo;
    private List<TripStopResponseDTO> tripStops;
    private VehicleResponseTripDTO vehicle;
    private LocalDateTime startDateTime;
    private int availableSeat;
    private int currentAvailableSeats;
    private String availableBaggage;
    private double seatPrice;
}
