package com.carpool.carpool.dto.driver;

import java.time.LocalDate;

import com.carpool.carpool.enums.licenseStatus.LicenseStatusEnum;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DriverPendingResponseDTO  {
    private Long driverId;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate licenseExpirationDate;
    private String licenseClass;
    private LicenseStatusEnum licenseStatus;
    private String frontLicensePhotoUrl;
    private String backLicensePhotoUrl;
}
