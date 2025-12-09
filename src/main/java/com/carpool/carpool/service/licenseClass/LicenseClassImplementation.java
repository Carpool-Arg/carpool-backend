package com.carpool.carpool.service.licenseClass;

import com.carpool.carpool.dto.licenseClass.LicenseClassResponseDTO;
import com.carpool.carpool.mappers.licenseClass.LicenseClassMapper;
import com.carpool.carpool.model.licenseClass.LicenseClass;
import com.carpool.carpool.repository.licenseClass.LicenseClassRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LicenseClassImplementation implements ILicenseClassService {

    private final LicenseClassRepository licenseClassRepository;
    private final LicenseClassMapper licenseClassMapper;

    @Override
    public Response<List<LicenseClassResponseDTO>> getAllLicenseClasses() {
        List<LicenseClass> licenseClasses = licenseClassRepository.findAll();

        List<LicenseClassResponseDTO> licenseClassResponseDTOs = licenseClasses.stream()
                .map(licenseClassMapper::convertLicenseClassToLicenseClassResponseDTO)
                .collect(Collectors.toList());
        return ResponseUtils.buildOKResponse(List.of("Lista de clases de licencias de conducir traidas con exito"), licenseClassResponseDTOs );
    }
}
