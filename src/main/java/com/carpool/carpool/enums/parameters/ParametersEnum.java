package com.carpool.carpool.enums.parameters;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Clase que contiene los KEY de configuracion cargados en la DB
 * <p>
 *   {@code DEFAULT_CITY_KEY}
 *   {@code MINIMUM_CITY_DISTANCE}
 *   {@code DISCOUNT_PERCENTAGE}
 *   {@code AVERAGE_SPEED_KMH}
 * </p>
 */
@Getter
@AllArgsConstructor
public enum ParametersEnum {
    DEFAULT_CITY_KEY("default-city-id"),
    MINIMUM_CITY_DISTANCE("minimum-city-distance"),
    DISCOUNT_PERCENTAGE("discount-percentage"),
	AVERAGE_SPEED_KMH("average-speed-kmh");
    private final String key;
}
