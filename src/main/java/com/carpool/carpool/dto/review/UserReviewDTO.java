package com.carpool.carpool.dto.review;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserReviewDTO {
    private Long id;
    private int stars;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime tripDate;
    private String completeName;
    private String profilePhotoUrl;
    private Long tripId;
}
