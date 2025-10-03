package com.carpool.carpool.dto.trip.tripStop;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripStopResponseDTO {
    private String cityName;
    private String observation;
    private int order;
    private boolean isStart;
    private boolean isDestination;
}
