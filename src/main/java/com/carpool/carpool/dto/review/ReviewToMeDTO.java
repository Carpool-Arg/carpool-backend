package com.carpool.carpool.dto.review;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewToMeDTO {
  private int stars;
  private LocalDateTime reviewDate;
  private LocalDateTime tripDate;
  private String description;
  private String completeName;
  private String profilePhotoUrl;
}
