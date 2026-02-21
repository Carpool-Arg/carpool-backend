package com.carpool.carpool.dto.review;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ReviewResponseDTO {
    private Long id;
    private Integer stars;
    private String description;
    private String reviewerName;
    private String targetName;
}
