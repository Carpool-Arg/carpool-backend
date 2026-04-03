package com.carpool.carpool.dto.driver;

import com.carpool.carpool.enums.licenseStatus.LicenseStatusEnum;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class DriverResponseDTO {
    private Long id;
    private String fullName;
    private Double rating;
    private LicenseStatusEnum licenseStatus;
    private String frontLicenseUrl;
    private String backLicenseUrl;
    private String city;
}
