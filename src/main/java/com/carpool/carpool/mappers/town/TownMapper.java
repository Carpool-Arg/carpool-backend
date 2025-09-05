package com.carpool.carpool.mappers.town;

import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.town.TownResponseDTO;
import com.carpool.carpool.model.province.town.Town;

@Component
public class TownMapper {
    public TownResponseDTO convertTownToTownResponseDTO(Town town){
        return TownResponseDTO.builder()
                .id(town.getId())
                .name(town.getName())
                .zipCode(town.getZipCode())
                .build();

    }
}
