package com.carpool.carpool.repository.driver;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.carpool.carpool.model.driver.Driver;

public interface DriverRepository extends JpaRepository<Driver, Long>{
    Optional<Driver> findByUserId(Long userId);

   @Query("""
        SELECT d FROM Driver d
        WHERE d.licenseStatus = 'PENDING'
    """)
    Page<Driver> findAllPendingLicenses(Pageable pageable);
}