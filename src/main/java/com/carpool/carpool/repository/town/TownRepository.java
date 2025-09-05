package com.carpool.carpool.repository.town;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.carpool.carpool.model.province.town.Town;

public interface TownRepository extends JpaRepository<Town, Long> {
   
 List<Town> findByNameStartingWithIgnoreCase(String name);

}
