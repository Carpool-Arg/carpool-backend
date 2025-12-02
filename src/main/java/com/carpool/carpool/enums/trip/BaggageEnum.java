package com.carpool.carpool.enums.trip;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Clase que contiene los tipos de equipajes.
 * <p>
 *     {@code NO_EQUIPAJE}
 *     {@code LIVIANO}
 *     {@code MEDIANO}
 *     {@code PESADO}
 * </p>
 */
@Getter
@AllArgsConstructor
public enum BaggageEnum {

    NO_EQUIPAJE("No equipaje"),
    LIVIANO("Liviano"),
    MEDIANO("Mediano"),
    PESADO("Pesado");

    private final String typeBaggage;

    public static boolean contains(String value) {
        for (BaggageEnum baggage : values()) {
            if (baggage.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
