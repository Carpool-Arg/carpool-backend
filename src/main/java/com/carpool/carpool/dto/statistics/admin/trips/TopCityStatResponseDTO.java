package com.carpool.carpool.dto.statistics.admin.trips;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TopCityStatResponseDTO {
  private List<TopCityStatDTO> cities;
  private long totalReservationsCount;
}
