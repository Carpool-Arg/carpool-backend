package com.carpool.carpool.dto.review;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyMadeReviewsResponseDTO {
    private long total;
    private List<UserReviewDTO> reviews;
}
