package com.carpool.carpool.mappers.city;

import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.city.CityResponseDTO;
import com.carpool.carpool.model.province.city.City;

@Component
public class CityMapper {
    public CityResponseDTO convertCityToCityResponseDTO(City city){
        return CityResponseDTO.builder()
                .id(city.getId())
                .name(city.getName())
                .zipCode(city.getZipCode())
                .build();

    }
}
