package com.carpool.carpool.dto.trip;

import java.time.LocalDateTime;
import java.util.List;

import com.carpool.carpool.dto.driver.DriverSearchResponseDTO;
import com.carpool.carpool.dto.trip.tripStop.TripStopResponseDTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripSearchResponseDTO {

    private DriverSearchResponseDTO driverInfo;
    private LocalDateTime startDateTime;
    private List<TripStopResponseDTO> tripStops;
    private int availableSeat;
    private double seatPrice;
    
}
