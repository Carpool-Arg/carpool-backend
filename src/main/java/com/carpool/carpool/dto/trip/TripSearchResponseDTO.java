package com.carpool.carpool.dto.trip;

import java.time.LocalDateTime;
import java.util.List;

import com.carpool.carpool.dto.driver.DriverSearchResponseDTO;
import com.carpool.carpool.dto.trip.tripStop.TripStopSearchResponseDTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripSearchResponseDTO {

    private Long tripId;
    private DriverSearchResponseDTO driverInfo;
    private LocalDateTime startDateTime;
    private List<TripStopSearchResponseDTO> tripStops;
    private int availableSeat;
    private double seatPrice;
    
}
