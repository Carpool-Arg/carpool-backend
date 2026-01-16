package com.carpool.carpool.dto.trip;

import java.util.List;

import com.carpool.carpool.dto.trip.tripStop.CurrentTripStopResponseDTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CurrentTripResponseDTO {
  private Long idTrip;
  private List<CurrentTripStopResponseDTO> tripStops;

}
