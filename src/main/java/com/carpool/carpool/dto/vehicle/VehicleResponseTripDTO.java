package com.carpool.carpool.dto.vehicle;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VehicleResponseTripDTO {
    
	private Long id;
    private String domain;
    private String vehicleTypeName;
    private String brand;
    private String model;
    private String color;

}
