package com.carpool.carpool.service.setting;

public interface IConfigService {
    
    /**
     * Obtiene el ID de la ciudad por defecto desde la configuracion.
     * @return El ID de la ciudad por defecto.
     * 
    */
    Long getDefaultCityId();
}
