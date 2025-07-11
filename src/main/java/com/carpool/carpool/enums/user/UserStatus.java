package com.carpool.carpool.enums.user;

/**
 * Clase que contiene los estados de un usuario.
 * <p>
 *     {@code PENDING_VERIFICATION} El usuario se registró pero no confirmó la validación del correo electrónico
 *     {@code PENDING_PROFILE} Registro parcial, el cual el usuario re registró mediante Google pero debe completar el registro.
 *     {@code ACTIVE} Usuario con registro completo y validación del correo electrónico.
 *     {@code SUSPENDED} Usuario suspendido.
 *     {@code DELETED} Usuario eliminado.
 * </p>
 */
public enum UserStatus {
    PENDING_VERIFICATION,
    PENDING_PROFILE,
    ACTIVE,
    SUSPENDED
}
