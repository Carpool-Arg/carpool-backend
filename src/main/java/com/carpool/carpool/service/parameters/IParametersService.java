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
     * Obtiene el procentaje del precio indicado por el chofer que se cobrara adicional por el uso de la aplicacion 
     * tanto a choferes como a pasajeros
     * @return El procentaje en numeros enteros
     *
     */
    int getDiscountPercentage();
}
