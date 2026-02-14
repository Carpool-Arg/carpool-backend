package com.carpool.carpool.dto.review;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DriverReviewResponseDTO {
  private Long reviewId;
  private int stars;
  private String description;
  private LocalDateTime createdAt;
}
