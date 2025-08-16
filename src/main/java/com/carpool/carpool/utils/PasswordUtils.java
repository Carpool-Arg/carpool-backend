package com.carpool.carpool.utils;

import com.carpool.carpool.exception.ConflictException;

public class PasswordUtils {

    /**
     * Metodo para comprobar que las contraseña y la confirmacion de la misma coinciden
     * @param userPassword la contraseña del usuario
     * @param userConfirmPassword la confirmacion de la contraseña del usuario
     * @throws ConflictException si no coinciden
     */
    public static void passwordsMatch(String userPassword, String userConfirmPassword){
        if(!userPassword.equals(userConfirmPassword)){
            throw new ConflictException("Las contraseñas ingresadas no coinciden");
        }
    }
}
