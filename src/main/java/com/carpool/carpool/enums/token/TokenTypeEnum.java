package com.carpool.carpool.enums.token;

/**
 * Clase que contiene los tipos de token.
 * <p>
 *     {@code ACTIVATION} Token para activar una cuenta.
 *     {@code EMAIL_CHANGE} Token para aprobar la solicitud de cambio de correo electronico
 * </p>
 */
public enum TokenTypeEnum {
    ACTIVATION,
    EMAIL_CHANGE,
    PASSWORD_CHANGE
}
