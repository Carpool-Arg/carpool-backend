package com.carpool.carpool.service.parameters;

import com.carpool.carpool.enums.parameters.ParametersEnum;
import org.springframework.stereotype.Service;

import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.model.parameters.ConfigParameters;
import com.carpool.carpool.repository.parameters.ParametersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor    
public class ParametersImplementation implements IParametersService {

    private final ParametersRepository parametersRepository;

    private static final String CONFIG_NOT_FOUND = "Clave de configuración '%s' no encontrada en la base de datos.";
    private static final String INVALID_NUMBER_FORMAT = "El valor de configuración '%s' en la base de datos no es un número válido: %s";

    @Override
    public Long getDefaultCityId() {
        String key = ParametersEnum.DEFAULT_CITY_KEY.getKey();

        // 1. Buscar el Setting en la DB por su clave
        ConfigParameters config = parametersRepository.findByKeyName(ParametersEnum.DEFAULT_CITY_KEY.getKey())
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CONFIG_NOT_FOUND, key)));

        // 2. Obtener el valor (es un String)
        String cityIdValue = config.getKeyValue();
        
        // 3. Convertir el valor a Long y manejar el error de formato
        try {
            return Long.valueOf(cityIdValue);
        } catch (NumberFormatException e) {
            // Esto ocurre si el valor en la DB es 'abc' en lugar de '409'
            throw new IllegalStateException(String.format(INVALID_NUMBER_FORMAT, key, cityIdValue), e);        
        }
    }

    @Override
    public int getMinimumCityDistance() {
        String key = ParametersEnum.MINIMUM_CITY_DISTANCE.getKey();

        // 1. Buscar el Setting en la DB por su clave
        ConfigParameters config = parametersRepository.findByKeyName(ParametersEnum.MINIMUM_CITY_DISTANCE.getKey())
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CONFIG_NOT_FOUND, key)));

        // 2. Obtener el valor (es un String)
        String minimumDistance = config.getKeyValue();

        // 3. Convertir el valor a int y manejar el error de formato
        try {
            return Integer.parseInt(minimumDistance);
        } catch (NumberFormatException e) {
           throw new IllegalStateException(String.format(INVALID_NUMBER_FORMAT, key, minimumDistance), e);
        }
    }

    @Override
    public int getMinimunPriceValue() {
        String key = ParametersEnum.MINIMUN_PRICE_VALUE.getKey();

        // 1. Buscar el Setting en la DB por su clave
        ConfigParameters config = parametersRepository.findByKeyName(ParametersEnum.MINIMUN_PRICE_VALUE.getKey())
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CONFIG_NOT_FOUND, key)));
        // 2. Obtener el valor (es un String)
        String minimumPriceValue = config.getKeyValue();

        // 3. Convertir el valor a int y manejar el error de formato
        try {
            return Integer.parseInt(minimumPriceValue);
        } catch (NumberFormatException e) {
           throw new IllegalStateException(String.format(INVALID_NUMBER_FORMAT, key, minimumPriceValue), e);
        }
    }
}