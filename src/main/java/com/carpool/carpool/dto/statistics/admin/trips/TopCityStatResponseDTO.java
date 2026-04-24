package com.carpool.carpool.dto.statistics.admin.trips;

import java.util.List;

import com.google.auto.value.AutoValue.Builder;

import lombok.Getter;

@Builder
@Getter
public class TopCityStatResponseDTO {
  private List<TopCityStatDTO> cities;
}
