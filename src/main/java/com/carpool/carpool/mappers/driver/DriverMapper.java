package com.carpool.carpool.mappers.driver;

import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.driver.DriverRequestDTO;
import com.carpool.carpool.model.driver.Driver;
import com.carpool.carpool.model.user.User;

@Component 
public class DriverMapper {
    public Driver convertDriverRequestDTOToDriver(DriverRequestDTO driverRequestDTO, User user) {
        return Driver.builder()
            .licenseClass(driverRequestDTO.getLicenseClass())
            .licenseExpirationDate(driverRequestDTO.getLicenseExpirationDate())
            .birthDate(driverRequestDTO.getBirthDate())
            .addressStreet(driverRequestDTO.getAddressStreet())
            .addressNumber(driverRequestDTO.getAddressNumber())
            .addressLocality(driverRequestDTO.getLocality())
            .user(user)
            .build();
    } 
}

