package com.carpool.carpool.repository.city;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.carpool.carpool.model.province.city.City;

public interface CityRepository extends JpaRepository<City, Long> {
   
 List<City> findByNameStartingWithIgnoreCase(String name);

}
