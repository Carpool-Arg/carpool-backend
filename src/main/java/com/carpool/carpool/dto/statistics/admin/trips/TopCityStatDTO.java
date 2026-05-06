package com.carpool.carpool.dto.statistics.admin.trips;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TopCityStatDTO {
  private String cityName;
  private Long reservationCount;
}
