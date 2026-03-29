package com.carpool.carpool.service.driver;

import java.util.List;

import com.carpool.carpool.dto.driver.DriverLicenseVerifyRequestDTO;
import com.carpool.carpool.dto.driver.DriverPendingResponseDTO;
import com.carpool.carpool.dto.driver.DriverRequestDTO;
import com.carpool.carpool.dto.security.token.TokenResponseDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.exception.ConflictException;

public interface IDriverService {
    
    /**
     * Metodo utilizado para almacenar un conductor en la base de datos. 
     * Se realizan controles para lanzar las excepciones correspondientes.
     * @param driverRequestDTO request con los datos del conductor a guardar
     * @return Response<Void> devolviendo el mensaje si el conductor fue creado
     * @throws ConflictException si el usuario es menor de edad, ya tiene un perfil de chofer, o no se encuentra el usuario o el rol correspondiente.
     */
    Response<TokenResponseDTO> saveDriver(DriverRequestDTO driverRequestDTO);

    /**
     * 
     * @return
     */
    Response<List<DriverPendingResponseDTO>> getPendingLicenses();
    
    /**
     * 
     * @param driverId
     * @param dto
     * @return
     */
    Response<Void> verifyLicense(Long driverId, DriverLicenseVerifyRequestDTO dto);

}
