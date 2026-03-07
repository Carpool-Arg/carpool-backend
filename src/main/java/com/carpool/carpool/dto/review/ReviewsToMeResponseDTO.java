package com.carpool.carpool.dto.review;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewsToMeResponseDTO {
  private double rating;
  private long total;
  private List<UserReviewDTO> reviews;
}
