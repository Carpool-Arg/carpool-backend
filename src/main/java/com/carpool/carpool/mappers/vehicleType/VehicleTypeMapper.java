package com.carpool.carpool.mappers.vehicleType;

import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.vehicleType.VehicleTypeResponseDTO;
import com.carpool.carpool.model.vehicleType.VehicleType;

@Component
public class VehicleTypeMapper {
    
    public VehicleTypeResponseDTO convertVehicleTypeToVehicleTypeResponseDTO(VehicleType vehicleType){
        return VehicleTypeResponseDTO.builder()
                .id(vehicleType.getId())
                .name(vehicleType.getName())
                .description(vehicleType.getDescription())
                .build();

    } 
}
