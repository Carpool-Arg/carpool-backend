package com.carpool.carpool.repository.province;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carpool.carpool.model.province.Province;

public interface ProvinceRepository extends JpaRepository<Province, Long> {
    Optional<Province> findByName(String name);
}
