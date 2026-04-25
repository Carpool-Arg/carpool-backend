package com.carpool.carpool.repository.parameters;



import org.springframework.data.jpa.repository.JpaRepository;
import com.carpool.carpool.model.parameters.ConfigParameters;

import java.util.Optional;

public interface ParametersRepository extends JpaRepository<ConfigParameters, Long> {

    /**
     * Busca una configuracion por su clave (key_name).
     * @param keyName La clave de configuracion (ej: 'app.trip.default-city-id').
     * @return Un Optional que contiene el objeto de configuracion si se encuentra.
     */
    Optional<ConfigParameters> findByKeyName(String keyName);
}

