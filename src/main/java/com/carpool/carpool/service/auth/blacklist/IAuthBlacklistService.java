package com.carpool.carpool.service.auth.blacklist;

import com.carpool.carpool.response.Response;

public interface IAuthBlacklistService {
    /**
     * Agrega un token JWT a la blacklist con un tiempo de expiración igual
     * al tiempo restante de vida del token.
     *
     * @param token token JWT completo (con prefijo "Bearer ")
     * @return response respuesta con mensaje y estado de la operación
     */
    Response<Void> blacklistToken(String token);

    /**
     * Verifica si un token JWT está en la blacklist.
     *
     * @param token token JWT limpio (sin el prefijo "Bearer ")
     * @return boolean true si el token está en blacklist, false si no
     */
    boolean isTokenBlacklisted(String token);
}
