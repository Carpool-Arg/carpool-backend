package com.carpool.carpool.dto.town;



import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TownResponseDTO {
    
    private Long id;
    private String name;
    private int zipCode;
}

