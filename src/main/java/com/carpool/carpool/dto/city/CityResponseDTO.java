package com.carpool.carpool.dto.city;



import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CityResponseDTO {
    
    private Long id;
    private String name;
    private int zipCode;
}

