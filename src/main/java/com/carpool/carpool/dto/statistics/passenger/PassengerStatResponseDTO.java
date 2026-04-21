package com.carpool.carpool.dto.statistics.passenger;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PassengerStatResponseDTO {
    private double historialTotal;
    private double kmFiltered; 
    private List<StatMetricPointDTO> historialByPeriod;
}
