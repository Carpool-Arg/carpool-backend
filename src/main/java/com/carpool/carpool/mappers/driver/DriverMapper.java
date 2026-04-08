package com.carpool.carpool.mappers.driver;

import com.carpool.carpool.model.licenseClass.LicenseClass;
import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.driver.DriverPendingResponseDTO;
import com.carpool.carpool.dto.driver.DriverRequestDTO;
import com.carpool.carpool.dto.driver.DriverResponseDTO;
import com.carpool.carpool.model.driver.Driver;
import com.carpool.carpool.model.province.city.City;
import com.carpool.carpool.model.user.User;

@Component
public class DriverMapper {
    public Driver convertDriverRequestDTOToDriver(DriverRequestDTO driverRequestDTO, User user, City city, LicenseClass licenseClass) {
        return Driver.builder()
                .licenseClass(licenseClass)
                .licenseExpirationDate(driverRequestDTO.getLicenseExpirationDate())
                .addressStreet(driverRequestDTO.getAddressStreet())
                .addressNumber(driverRequestDTO.getAddressNumber())
                .city(city)
                .user(user)
                .build();
    }

    public DriverPendingResponseDTO convertDriverToDriverPendingResponseDTO(
        Driver driver, String frontUrl, String backUrl){

            return DriverPendingResponseDTO.builder()
                .driverId(driver.getId())
                .userId(driver.getUser().getId())
                .fullName(driver.getUser().getName() + " " + driver.getUser().getLastname())
                .email(driver.getUser().getEmail())
                .phone(driver.getUser().getPhone())
                .licenseExpirationDate(driver.getLicenseExpirationDate())
                .licenseClass(driver.getLicenseClass().getName())
                .licenseStatus(driver.getLicenseStatus())
                .frontLicensePhotoUrl(frontUrl)
                .backLicensePhotoUrl(backUrl)
                .build();
    }

    public DriverResponseDTO convertDriverToDriverResponseDTO(Driver driver, String frontUrl, String backUrl){
        return DriverResponseDTO.builder()
                .id(driver.getId())
                .fullName(driver.getUser().getName() + " " + driver.getUser().getLastname())
                .rating(driver.getRating())
                .licenseStatus(driver.getLicenseStatus())
                .licenseExpirationDate(driver.getLicenseExpirationDate())
                .addressStreetAndNumber(driver.getAddressStreet() + " " + driver.getAddressNumber())
                .frontLicenseUrl(frontUrl)
                .backLicenseUrl(backUrl)
                .city(driver.getCity().getName())
                .build();
    }
}

