package com.carpool.carpool.service.setting;

import com.carpool.carpool.enums.setting.SettingEnum;
import org.springframework.stereotype.Service;

import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.model.setting.ConfigurationSetting;
import com.carpool.carpool.repository.setting.ConfigurationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor    
public class SettingImplementation implements ISettingService {

    private final ConfigurationRepository configurationRepository;

    @Override
    public Long getDefaultCityId() {
        
        // 1. Buscar el Setting en la DB por su clave
        ConfigurationSetting config = configurationRepository.findByKeyName(SettingEnum.DEFAULT_CITY_KEY.getKey())
            .orElseThrow(() -> new ResourceNotFoundException("Clave de configuración '" + SettingEnum.DEFAULT_CITY_KEY.getKey() + "' no encontrada en la base de datos."));
            
        // 2. Obtener el valor (es un String)
        String cityIdValue = config.getKeyValue();
        
        // 3. Convertir el valor a Long y manejar el error de formato
        try {
            return Long.valueOf(cityIdValue);
        } catch (NumberFormatException e) {
            // Esto ocurre si el valor en la DB es 'abc' en lugar de '409'
            throw new IllegalStateException("El valor de configuración '" + SettingEnum.DEFAULT_CITY_KEY.getKey() + "' en la base de datos no es un ID numérico válido: " + cityIdValue, e);
        }
    }

    @Override
    public int getMinimumCityDistance() {
        // 1. Buscar el Setting en la DB por su clave
        ConfigurationSetting config = configurationRepository.findByKeyName(SettingEnum.MINIMUM_CITY_DISTANCE.getKey())
                .orElseThrow(() -> new ResourceNotFoundException("Clave de configuración '" + SettingEnum.MINIMUM_CITY_DISTANCE.getKey() + "' no encontrada en la base de datos."));

        // 2. Obtener el valor (es un String)
        String minimumDistance = config.getKeyValue();

        // 3. Convertir el valor a int y manejar el error de formato
        try {
            return Integer.parseInt(minimumDistance);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "El valor de configuración '" + SettingEnum.DEFAULT_CITY_KEY.getKey() +
                            "' en la base de datos no es un número válido: " + minimumDistance, e
            );
        }
    }
}