package com.carpool.carpool.dto.vehicle;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleResponseDTO {
    
    private Long id;
    private String domain;
    private String brand;
    private String model;
    private Integer year;
    private String color;
    private Integer availableSeats;
    private Long vehicleTypeId; 
    private String vehicleTypeName; 
    private Long driverId; 
    private LocalDateTime createdAt;
    
}
