package com.carpool.carpool.repository.setting;



import org.springframework.data.jpa.repository.JpaRepository;
import com.carpool.carpool.model.setting.ConfigurationSetting;

import java.util.Optional;

public interface ConfigurationRepository extends JpaRepository<ConfigurationSetting, Long> {

    /**
     * Busca una configuracion por su clave (key_name).
     * @param keyName La clave de configuracion (ej: 'app.trip.default-city-id').
     * @return Un Optional que contiene el objeto de configuracion si se encuentra.
     */
    Optional<ConfigurationSetting> findByKeyName(String keyName);
}

