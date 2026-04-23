package com.carpool.carpool.dto.statistics.driver;

import java.util.List;

import com.carpool.carpool.dto.statistics.StatMetricPointDTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DriverStatResponseDTO {
    private double historialTotal;
    private double totalFiltered; 
    private List<StatMetricPointDTO> historialByPeriod;
}
