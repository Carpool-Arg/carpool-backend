package com.carpool.carpool.dto.trip;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PassengerTripDTO {
  private Long idPassenger;
  private String passengerName;
  private String passengerLastname;
  private String profilePhotoUrl;
}
