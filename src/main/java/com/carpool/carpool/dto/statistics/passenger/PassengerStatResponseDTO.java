package com.carpool.carpool.dto.statistics.passenger;

import java.util.List;

import com.carpool.carpool.dto.statistics.StatMetricPointDTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PassengerStatResponseDTO {
    private double historialTotal;
    private double totalFiltered; 
    private List<StatMetricPointDTO> historialByPeriod;
}
