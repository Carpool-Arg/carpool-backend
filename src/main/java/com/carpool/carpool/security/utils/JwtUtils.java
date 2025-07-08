package com.carpool.carpool.security.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import com.carpool.carpool.model.user.User;

import static com.carpool.carpool.security.config.TokenJwtConfig.*;

import java.util.Date;

/**
 * Clase utilitaria para operaciones relacionadas con JSON Web Tokens (JWT).
 * Incluye generación, validación y extracción de información desde tokens.
 */
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

    /**
     * Genera un nuevo token JWT firmado.
     *
     * @param username el nombre de usuario a incluir como "subject"
     * @param time duración del token en milisegundos
     * @param claims los claims que se desean reutilizar (pueden venir de otro token)
     * @return el token JWT generado y firmado
     */
    public String generateToken(String username, int time, Claims claims){
        return Jwts.builder()
                .subject(username)
                .claims(claims)
                .expiration(new Date(System.currentTimeMillis() + time)) // 7 dias de duracion 
                .issuedAt(new Date()) // Fecha de creación
                .signWith(SECRET_KEY) // Firma con clave secreta
                .compact();
    }

    /**
     * Extrae el nombre de usuario desde el token JWT.
     *
     * @param token el token JWT
     * @return el valor del campo "subject" (username)
     */
    public String extractUsername(String token){
        final Claims claims = Jwts.parser()
            .verifyWith(SECRET_KEY)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        return claims.getSubject();
    }

    /**
     * Verifica si un token es válido para el usuario dado.
     * Un token es válido si el username coincide y no está expirado.
     *
     * @param token el token JWT
     * @param user el usuario contra el que se valida el token
     * @return true si el token es válido, false si no
     */
    public boolean isTokenValid(final String token, final User user){
        final String username = extractUsername(token);
        return (username.equals(user.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Verifica si un token JWT ya expiró.
     *
     * @param token el token JWT
     * @return true si el token ya expiró, false si aún es válido
     */
    public boolean isTokenExpired(final String token){
        return getTokenExpiration(token)<=0;
    }
}
