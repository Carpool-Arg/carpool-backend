package com.carpool.carpool.enums.media;

/**
 * Clase que contiene las categoria que puede asumir un recurso.
 * <p>
 *     {@code PROFILE} Recursos que pertenecen al perfil del usuario. Como del perfil solamente se podria subir la foto de perfil, se incorporó
 *     la restriccion de que solo existe un registro {@code PROFILE} por usuario en la base de datos.
 * </p>
 */
public enum CategoryMediaEnum {
    PROFILE
}
