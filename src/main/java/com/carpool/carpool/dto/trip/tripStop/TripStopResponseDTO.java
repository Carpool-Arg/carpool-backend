package com.carpool.carpool.dto.trip.tripStop;

import java.time.LocalDateTime;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripStopResponseDTO {
    private Long tripStopId;
    private Long cityId;
    private String cityName;
    private String observation;
    private int order;
    private boolean start;
    private boolean destination;
    private LocalDateTime estimatedArrivalDateTime;
}
