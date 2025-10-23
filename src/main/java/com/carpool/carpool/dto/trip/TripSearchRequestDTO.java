package com.carpool.carpool.dto.trip;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripSearchRequestDTO {
    
    private Long originCityId;
    private Long destinationCityId;

    private Long userCityId; 

    private LocalDate departureDate;
    private Double minPrice;
    private Double maxPrice;
    
    private Boolean orderByDriverRating;
    
}
