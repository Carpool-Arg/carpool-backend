package com.carpool.carpool.dto.vehicle;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleOnlyResponseDTO {

    private Long id;
    private String brand;
    private String model;
    private Integer year;
    private String color;
    private Integer availableSeats;
    
}
