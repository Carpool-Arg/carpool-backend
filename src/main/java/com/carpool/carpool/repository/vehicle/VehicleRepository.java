package com.carpool.carpool.repository.vehicle;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carpool.carpool.model.driver.Driver;
import com.carpool.carpool.model.vehicle.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByDomain(String domain);
    Optional<Vehicle> findByIdAndDriver(Long id, Driver driver);
    List<Vehicle> findByDriverAndDeletedAtIsNull(Driver driver);
}
