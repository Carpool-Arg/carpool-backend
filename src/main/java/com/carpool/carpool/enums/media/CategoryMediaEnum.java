package com.carpool.carpool.enums.media;

/**
 * Clase que contiene las categoria que puede asumir un recurso.
 * <p>
 *     {@code PROFILE} Recursos que pertenecen al perfil del usuario. Como del perfil solamente se podria subir la foto de perfil, se incorporó
 *     la restriccion de que solo existe un registro {@code PROFILE} por usuario en la base de datos.
 * 
*      {@code LICENSE_FRONT} Recursos que corresponden a la parte frontal de la licencia de conducir del usuario.
 *     Este recurso se utiliza para validar la identidad y los datos visibles en la licencia.
 *     
 *     {@code LICENSE_BACK} Recursos que corresponden al reverso de la licencia de conducir del usuario.
 *     Este recurso se utiliza para complementar la validación, incluyendo información adicional o códigos presentes en la parte trasera.

 * </p>
 */
public enum CategoryMediaEnum {
    PROFILE,
    LICENSE_FRONT,
    LICENSE_BACK
}
