package com.carpool.carpool.dto.statistics;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatMetricPointDTO {
    private String label;
    private Double value;
}
