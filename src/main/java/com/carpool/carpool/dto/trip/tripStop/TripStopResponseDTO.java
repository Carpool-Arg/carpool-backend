package com.carpool.carpool.dto.trip.tripStop;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TripStopResponseDTO {
    private String cityName;
    private String observation;
}
