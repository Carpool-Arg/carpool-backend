package com.carpool.carpool.dto.trip;

import com.carpool.carpool.dto.review.TripPassengerReviewDTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PassengerTripDTO {
  private Long idPassenger;
  private String passengerName;
  private String passengerLastname;
  private String profilePhotoUrl;
  private TripPassengerReviewDTO review;
}
