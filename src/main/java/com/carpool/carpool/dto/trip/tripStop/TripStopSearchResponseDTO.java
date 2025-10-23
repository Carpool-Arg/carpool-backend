package com.carpool.carpool.dto.trip.tripStop;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripStopSearchResponseDTO {
    private String cityName; 
    private String observation;
    private boolean start;
    private boolean destination;
    private LocalDateTime estimatedArrivalDateTime;
}
