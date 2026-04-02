package com.carpool.carpool.service.driver;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

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
     * @param frontPhoto foto del frente del carnet de conducir
     * @param backPhoto foto del dorso del carnet de conducir
     * @return Response<Void> devolviendo el mensaje si el conductor fue creado
     * @throws ConflictException si el usuario es menor de edad, ya tiene un perfil de chofer, o no se encuentra el usuario o el rol correspondiente.
     * 
     */
    Response<TokenResponseDTO> saveDriver(DriverRequestDTO driverRequestDTO, MultipartFile frontPhoto, MultipartFile backPhoto);

    /** 
     * Metodo utilizado para obtener la lista de conductores con licencias pendientes de verificación.
      * @return Response<List<DriverPendingResponseDTO>> devolviendo la lista de conductores con licencias pendientes de verificación.
     */
    Response<List<DriverPendingResponseDTO>> getPendingLicenses();
    
    /**
     * Metodo utilizado para verificar la licencia de un conductor. Se realizan controles para lanzar las excepciones correspondientes.
     * @param driverId id del conductor a verificar
     * @param dto request con los datos de la verificación de la licencia
     * @return Response<Void> devolviendo el mensaje si la licencia fue verificada
      * @throws ConflictException si no se encuentra el conductor o la licencia ya fue verificada.
      *
     */
    Response<Void> verifyLicense(Long driverId, DriverLicenseVerifyRequestDTO dto);

}
