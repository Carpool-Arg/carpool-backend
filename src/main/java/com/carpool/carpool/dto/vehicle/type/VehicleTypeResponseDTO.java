package com.carpool.carpool.dto.vehicle.type;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data   
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleTypeResponseDTO {
    
    private Long id;
    private String name;
    private String description; 
}
