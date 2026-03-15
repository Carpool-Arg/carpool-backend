package com.carpool.carpool.dto.trip;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripPassengersResponseDTO {
  private List<PassengerTripDTO> passengers;
}
