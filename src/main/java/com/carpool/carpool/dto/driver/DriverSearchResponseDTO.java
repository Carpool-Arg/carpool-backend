package com.carpool.carpool.dto.driver;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DriverSearchResponseDTO {
    
    private String fullName;
    private String profileImageUrl;
    private double rating;
}
