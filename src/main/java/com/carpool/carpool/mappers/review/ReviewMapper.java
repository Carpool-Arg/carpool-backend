package com.carpool.carpool.mappers.review;

import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.review.DriverReviewResponseDTO;
import com.carpool.carpool.model.review.Review;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewMapper {


  public DriverReviewResponseDTO convertReviewToDriverReviewResponseDTO(Review review){
    return DriverReviewResponseDTO.builder()
      .reviewId(review.getId())
      .stars(review.getStars())
      .createdAt(review.getCreatedAt())
      .description(review.getDescription())
    .build();

  }
}
