package com.carpool.carpool.repository.driver;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.carpool.carpool.model.driver.Driver;

public interface DriverRepository extends JpaRepository<Driver, Long>{
    
    Optional<Driver> findByUserId(Long userId);
    
}


