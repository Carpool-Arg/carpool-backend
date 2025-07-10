package com.carpool.carpool.repository.city;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.carpool.carpool.model.city.City;

public interface CityRepository extends JpaRepository<City, Long>{
    Optional<City> findByName(String name);
    Optional<City> findByZipCode(String zipCode); 
}
