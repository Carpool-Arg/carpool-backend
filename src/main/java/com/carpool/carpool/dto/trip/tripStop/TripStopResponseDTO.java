package com.carpool.carpool.dto.trip.tripStop;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripStopResponseDTO {
    private String cityName;
    private String observation;
}
