package com.carpool.carpool.service.setting;

import org.springframework.stereotype.Service;

import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.model.setting.ConfigurationSetting;
import com.carpool.carpool.repository.setting.ConfigurationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor    
public class ConfigImplementation implements IConfigService {

    private final ConfigurationRepository configurationRepository;
    
    //Clave de la localidad por defecto
    private static final String DEFAULT_CITY_KEY = "default-city-id";

    @Override
    public Long getDefaultCityId() {
        
        // 1. Buscar el Setting en la DB por su clave
        ConfigurationSetting config = configurationRepository.findByKeyName(DEFAULT_CITY_KEY)
            .orElseThrow(() -> new ResourceNotFoundException("Clave de configuración '" + DEFAULT_CITY_KEY + "' no encontrada en la base de datos."));
            
        // 2. Obtener el valor (es un String)
        String cityIdValue = config.getKeyValue();
        
        // 3. Convertir el valor a Long y manejar el error de formato
        try {
            return Long.valueOf(cityIdValue);
        } catch (NumberFormatException e) {
            // Esto ocurre si el valor en la DB es 'abc' en lugar de '409'
            throw new IllegalStateException("El valor de configuración '" + DEFAULT_CITY_KEY + "' en la base de datos no es un ID numérico válido: " + cityIdValue, e);
        }
    }
    
}