package com.carpool.carpool.service.auth;

import com.carpool.carpool.dto.security.token.TokenResponseDTO;
import com.carpool.carpool.response.Response;

public interface IAuthService {
    /**
     * Genera un nuevo access token a partir de un refresh token válido.
     *
     * @param authHeader Encabezado Authorization con el refresh token (formato: "Bearer <token>")
     * @return Response<TokenResponseDTO> con el nuevo access token y el mismo refresh token
     * @throws IllegalArgumentException si el token es inválido, está en la blacklist o ha expirado
     */
    Response<TokenResponseDTO> refreshToken(String authHeader);

    /**
     * Verifica la validez de un token JWT contenido en el encabezado Authorization.
     * @param authHeader el valor del encabezado Authorization que debe comenzar con "Bearer " seguido del token JWT.
     * @return response respuesta con mensaje y estado de la operación
     */
    Response<Void> verifyToken(String authHeader);
}
