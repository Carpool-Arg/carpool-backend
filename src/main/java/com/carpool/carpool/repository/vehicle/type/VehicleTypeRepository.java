package com.carpool.carpool.repository.vehicle.type;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carpool.carpool.model.vehicle.type.VehicleType;

public interface VehicleTypeRepository extends JpaRepository<VehicleType, Long>{
    
}
