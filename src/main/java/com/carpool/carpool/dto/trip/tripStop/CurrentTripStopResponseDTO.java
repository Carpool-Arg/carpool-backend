package com.carpool.carpool.dto.trip.tripStop;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CurrentTripStopResponseDTO {
  TripStopResponseDTO tripStop; 
  LocalDateTime arrivalDateTime;
  double distanceFromPrevious;
}
