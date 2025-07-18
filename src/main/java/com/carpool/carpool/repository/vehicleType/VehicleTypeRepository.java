package com.carpool.carpool.repository.vehicleType;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carpool.carpool.model.vehicleType.VehicleType;

public interface VehicleTypeRepository extends JpaRepository<VehicleType, Long>{
    
}
