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
    
    TRIP("Viaje"),
    RESERVATION("Reserva");

    private final String typeScope;
}
