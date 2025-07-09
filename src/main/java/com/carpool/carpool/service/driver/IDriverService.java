package com.carpool.carpool.service.driver;

import com.carpool.carpool.dto.driver.DriverRequestDTO;
import com.carpool.carpool.response.Response;

public interface IDriverService {
    
    /**
     * Metodo utilizado para almacenar un conductor en la base de datos. 
     * Se realizan controles para lanzar las excepciones correspondientes.
     * @param driverRequestDTO request con los datos del conductor a guardar
     * @return Response<Void> devolviendo el mensaje si el conductor fue creado
     */
    Response<Void> saveDriver(DriverRequestDTO driverRequestDTO);

}
