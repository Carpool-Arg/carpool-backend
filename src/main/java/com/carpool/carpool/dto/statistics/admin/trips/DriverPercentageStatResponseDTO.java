package com.carpool.carpool.dto.statistics.admin.trips;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DriverPercentageStatResponseDTO {
  private double driverPercentage;
}
