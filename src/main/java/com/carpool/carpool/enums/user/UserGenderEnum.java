package com.carpool.carpool.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Enum que contiene los géneros posibles de un usuario.
 * <p>
 * {@code MALE} Representa el género masculino.
 * {@code FEMALE} Representa el género femenino.
 * {@code UNSPECIFIED} Representa un género no especificado.
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum UserGenderEnum {
    MALE(1),
    FEMALE(2),
    UNSPECIFIED(3);

    private Integer code;

    /**
     * Busca el enum por su código.
     * @param code El código del género.
     * @return El enum correspondiente al código, o null si no se encuentra.
     */
    public static UserGenderEnum fromCode(Integer code) {
        // Se valida si el código es nulo, lo cual es posible si se recibe de un DTO, por ejemplo.
        if (code == null) {
            return null;
        }
        for (UserGenderEnum gender : UserGenderEnum.values()) {
            if (gender.code == code) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Código de género inválido: " + code);
    }
}