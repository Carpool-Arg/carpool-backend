package com.carpool.carpool.enums.state;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Clase que contiene los tipos de ámbitos.
 * <p>
 *   {@code TRIP}
 * 
 * </p>
 */

@Getter
@AllArgsConstructor
public enum ScopeEnum {
    
    TRIP("Viaje");

    private final String typeScope;

    public static boolean contains(String value) {
        for (ScopeEnum scope : values()) {
            if (scope.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

}
