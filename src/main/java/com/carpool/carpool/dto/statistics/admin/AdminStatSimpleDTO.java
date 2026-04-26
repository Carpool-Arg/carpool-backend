package com.carpool.carpool.dto.statistics.admin;

import java.util.List;

import com.carpool.carpool.dto.statistics.StatMetricPointDTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminStatSimpleDTO {
    private double historicalTotal; 
    private double totalFiltered; 
    private List<StatMetricPointDTO> historialByPeriod;

}
