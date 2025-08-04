package com.carpool.carpool.mappers.vehicle;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.vehicle.VehicleOnlyResponseDTO;
import com.carpool.carpool.dto.vehicle.VehicleRequestDTO;
import com.carpool.carpool.dto.vehicle.VehicleResponseDTO;
import com.carpool.carpool.dto.vehicle.VehicleUpdateRequestDTO;
import com.carpool.carpool.model.driver.Driver;
import com.carpool.carpool.model.vehicle.Vehicle;
import com.carpool.carpool.model.vehicle.type.VehicleType;

@Component
public class VehicleMapper {
    
    public Vehicle convertVehicleRequestDTOToVehicle(VehicleRequestDTO vehicleRequestDTO, Driver driver, VehicleType vehicleType) {
        return Vehicle.builder()
            .domain(vehicleRequestDTO.getDomain())
            .brand(vehicleRequestDTO.getBrand())
            .model(vehicleRequestDTO.getModel())
            .year(vehicleRequestDTO.getYear())
            .color(vehicleRequestDTO.getColor())
            .availableSeats(vehicleRequestDTO.getAvailableSeats())
            .luggageCapacity(vehicleRequestDTO.getLuggageCapacity())
            .driver(driver)
            .vehicleType(vehicleType)
            .build();
    }

    public Vehicle convertVehicleUpdateRequestDTOToVehicle(VehicleUpdateRequestDTO vehicleUpdateRequestDTO, Vehicle existingVehicle) {
        existingVehicle.setBrand(vehicleUpdateRequestDTO.getBrand());
        existingVehicle.setModel(vehicleUpdateRequestDTO.getModel());
        existingVehicle.setYear(vehicleUpdateRequestDTO.getYear());
        existingVehicle.setColor(vehicleUpdateRequestDTO.getColor());
        existingVehicle.setAvailableSeats(vehicleUpdateRequestDTO.getAvailableSeats());
        existingVehicle.setLuggageCapacity(vehicleUpdateRequestDTO.getLuggageCapacity());
        return existingVehicle;
    }

    public VehicleResponseDTO convertVehicleToVehicleResponseDTO(Vehicle vehicle) {
        return VehicleResponseDTO.builder()
            .id(vehicle.getId())
            .domain(vehicle.getDomain())
            .brand(vehicle.getBrand())
            .model(vehicle.getModel())
            .year(vehicle.getYear())
            .color(vehicle.getColor())
            .availableSeats(vehicle.getAvailableSeats())
            .luggageCapacity(vehicle.getLuggageCapacity())
            .vehicleTypeId(vehicle.getVehicleType().getId())
            .vehicleTypeName(vehicle.getVehicleType().getName())
            .driverId(vehicle.getDriver().getId())
            .createdAt(vehicle.getCreated_at())
            .build();
    }

    /*
     * Convierte una lista de objetos Vehicle a una lista de objetos VehicleResponseDTO.
     * Utiliza el método convertVehicleToVehicleResponseDTO para cada elemento de la lista.
     */
    public List<VehicleResponseDTO> convertVehicleListToVehicleResponseDTOList(List<Vehicle> vehicles) {
        return vehicles.stream()
                .map(this::convertVehicleToVehicleResponseDTO)
                .collect(Collectors.toList());
    }

    public VehicleOnlyResponseDTO convertVehicleToVehicleOnlyResponseDTO(Vehicle vehicle) {
        return VehicleOnlyResponseDTO.builder()
            .id(vehicle.getId())
            .brand(vehicle.getBrand())
            .model(vehicle.getModel())
            .year(vehicle.getYear())
            .color(vehicle.getColor())
            .availableSeats(vehicle.getAvailableSeats())
            .luggageCapacity(vehicle.getLuggageCapacity())
            .build();
    }
}
