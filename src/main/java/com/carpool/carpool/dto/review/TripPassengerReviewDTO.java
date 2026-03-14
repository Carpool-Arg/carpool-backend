package com.carpool.carpool.dto.review;

import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripPassengerReviewDTO {
  private int stars;
  private String description;
}
