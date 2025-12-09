package com.carpool.carpool.dto.licenseClass;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LicenseClassResponseDTO {
    private Long id;
    private String name;
}
