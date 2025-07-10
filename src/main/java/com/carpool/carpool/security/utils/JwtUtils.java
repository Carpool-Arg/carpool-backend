package com.carpool.carpool.security.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;
import java.util.Date;

import static com.carpool.carpool.security.config.TokenJwtConfig.*;


/**
 * Clase utilitaria para operaciones relacionadas con JSON Web Tokens (JWT).
 * Incluye generación, validación y extracción de información desde tokens.
 */
@Component
public class JwtUtils {

    /* -------------------------------------------------------------------------- */
    /*                   Metodos para el manejo de refresh token                  */
    /* -------------------------------------------------------------------------- */

    /**
     * Genera un nuevo refresh token JWT firmado, con duración de 7 días.
     *
     * @param username el nombre de usuario a incluir como "subject"
     * @param claims los claims que se desean reutilizar (pueden venir de otro token)
     * @return el token JWT generado y firmado
    */
    public String generateRefreshToken(String username, Claims claims){
        return Jwts.builder()
                .subject(username)
                .claims(claims)
                .expiration(new Date(System.currentTimeMillis() + 604800000)) // 7 dias de duracion
                .issuedAt(new Date()) // Fecha de creación
                .signWith(SECRET_KEY_REFRESH) // Firma con clave secreta (distinta de la del access token)
                .compact();
    }

    /**
     * Obtiene el tiempo restante de vida de un refresh token JWT en segundos.
     *
     * @param token el token JWT en formato String
     * @return long tiempo restante en segundos antes de que expire el token;
     */
    public long getRefreshTokenExpiration(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(SECRET_KEY_REFRESH)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        long expirationTime = claims.getExpiration().getTime(); // tiempo de expiración en ms desde Epoch
        long now = System.currentTimeMillis(); // tiempo actual en ms desde Epoch

        return expirationTime - now ; // en milisegundos
    }

    /**
     * Extrae el nombre de usuario desde el token JWT.
     *
     * @param token el token JWT
     * @return el valor del campo "subject" (username)
     */
    public String extractUsernameRefreshToken(String token){
        final Claims claims = Jwts.parser()
            .verifyWith(SECRET_KEY_REFRESH)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        return claims.getSubject();
    }

    /* -------------------------------------------------------------------------- */
    /*                   Metodos para el manejo del access token                  */
    /* -------------------------------------------------------------------------- */

    /*
     * Esta seccion tiene menos metodos que la del refresh token debido a que la comprobacion del access
     * se hace en la clase JWtValidationFilter
    */

    /**
     * Genera un nuevo access token JWT firmado, con duración de 1 día.
     *
     * @param username el nombre de usuario a incluir como "subject"
     * @param claims los claims que se desean reutilizar (pueden venir de otro token)
     * @return el token JWT generado y firmado
    */
    public String generateAccessToken(String username, Claims claims){
        return Jwts.builder()
                .subject(username)
                .claims(claims)
                .expiration(new Date(System.currentTimeMillis() + 86400000)) //  1 dia de duracion
                .issuedAt(new Date()) // Fecha de creación
                .signWith(SECRET_KEY_ACCESS) // Firma con clave secreta
                .compact();
    }

    /**
     * Obtiene el tiempo restante de vida de un access token JWT en segundos.
     *
     * @param token el token JWT en formato String
     * @return long tiempo restante en segundos antes de que expire el token;
     */
    public long getAccessTokenExpiration(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(SECRET_KEY_ACCESS)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        long expirationTime = claims.getExpiration().getTime(); // tiempo de expiración en ms desde Epoch
        long now = System.currentTimeMillis(); // tiempo actual en ms desde Epoch

        return (expirationTime - now) / 1000; // en segundos
    }

    /**
     * Valida si un access token JWT es válido.
     *
     * Verifica tanto la firma del token con la clave secreta correspondiente,
     * como que el token no haya expirado (campo 'exp').
     *
     * @param token el access token JWT en formato String (sin el prefijo "Bearer ")
     * @return true si el token es válido (firma correcta y no expirado); false en cualquier otro caso
     */
    public boolean isValidAccessToken(String token) {
        try {
            // Intenta parsear y verificar la firma del token con la clave del access token
            Claims claims = Jwts.parser()
                    .verifyWith(SECRET_KEY_ACCESS)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Verificar si el token está expirado
            Date expiration = claims.getExpiration();
            return expiration.after(new Date());

        } catch (Exception e) {
            // Si hay cualquier excepción (firma inválida, token corrupto, etc.), el token es inválido
            return false;
        }
    }
}
