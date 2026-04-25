package com.carpool.carpool.dto.driver;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class LicenseUrlsResponse {
    private String frontLicenseUrl;
    private String backLicenseUrl;
}
