package com.carpool.carpool.enums.setting;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Clase que contiene los KEY de configuracion cargados en la DB
 * <p>
 *   {@code DEFAULT_CITY_KEY}
 *   {@code MINIMUM_CITY_DISTANCE}
 *
 * </p>
 */
@Getter
@AllArgsConstructor
public enum SettingEnum {
    DEFAULT_CITY_KEY("default-city-id"),
    MINIMUM_CITY_DISTANCE("minimum-city-distance"),
    MINIMUN_PRICE_VALUE("minimun-price-value");
    private final String key;
}
