package com.carpool.carpool.service.licenseClass;

import com.carpool.carpool.dto.licenseClass.LicenseClassResponseDTO;
import com.carpool.carpool.response.Response;

import java.util.List;

public interface ILicenseClassService {
    Response<List<LicenseClassResponseDTO>> getAllLicenseClasses();
}
