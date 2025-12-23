package com.carpool.carpool.service.parameters;

public interface IParametersService {
    
    /**
     * Obtiene el ID de la ciudad por defecto desde la configuracion.
     * @return El ID de la ciudad por defecto.
     * 
    */
    Long getDefaultCityId();

    /**
     * Obtiene el la distancia minima permitida para la búsqueda de ciudad mediante coordenadas
     * @return La distancia minima permitida en KM
     *
     */
    int getMinimumCityDistance();

    /**
     * Obtiene el valor minimo permitido para publicar un viaje
     * @return El valor minimo en moneda local
     *
     */
    int getMinimunPriceValue();
}
