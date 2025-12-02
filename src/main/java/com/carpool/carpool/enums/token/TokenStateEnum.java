package com.carpool.carpool.enums.token;

/**
 * Clase que contiene los estados de un token.
 * <p>
 *     {@code PENDING} Token que todavia no se usó ni expiró.
 *     {@code USED} Token que ya fue usado.
 *     {@code EXPIRED} Token que expiró
 * </p>
 */
public enum TokenStateEnum {
    PENDING,
    USED,
    EXPIRED,
    ACTIVE
}
