package com.carpool.carpool.dto.statistics.passenger;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Co2StatResponseDTO {
    private double totalCo2Saved;
}
