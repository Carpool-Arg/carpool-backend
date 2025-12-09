package com.carpool.carpool.mappers.licenseClass;

import com.carpool.carpool.dto.licenseClass.LicenseClassResponseDTO;
import com.carpool.carpool.model.licenseClass.LicenseClass;
import org.springframework.stereotype.Component;

@Component
public class LicenseClassMapper {
    public LicenseClassResponseDTO convertLicenseClassToLicenseClassResponseDTO(LicenseClass licenseClass){
        return LicenseClassResponseDTO.builder()
                .id(licenseClass.getId())
                .name(licenseClass.getName())
                .build();
    }
}