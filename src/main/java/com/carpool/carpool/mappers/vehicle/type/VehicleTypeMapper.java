package com.carpool.carpool.mappers.vehicle.type;

import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.vehicle.type.VehicleTypeResponseDTO;
import com.carpool.carpool.model.vehicle.type.VehicleType;

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
