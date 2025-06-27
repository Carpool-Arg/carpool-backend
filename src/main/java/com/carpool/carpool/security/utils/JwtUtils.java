package com.carpool.carpool.security.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;
import static com.carpool.carpool.security.config.TokenJwtConfig.*;

@Component
public class JwtUtils {

    /**
     * Obtiene el tiempo restante de vida de un token JWT en segundos.
     *
     * @param token el token JWT en formato String
     * @return long tiempo restante en segundos antes de que expire el token;
     */
    public long getTokenExpiration(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        long expirationTime = claims.getExpiration().getTime(); // tiempo de expiración en ms desde Epoch
        long now = System.currentTimeMillis(); // tiempo actual en ms desde Epoch

        return (expirationTime - now) / 1000; // en segundos
    }
}
