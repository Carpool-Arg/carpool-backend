package com.carpool.carpool.security.utils;

import com.carpool.carpool.security.filter.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;

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

    /**
     * Genera token JWT para un usuario.
     * @param username nombre de usuario
     * @param authorities lista de autoridades (roles/permisos) concedidas al usuario
     * @return Token JWT como {@link String}
     * @throws RuntimeException si ocurre un error al generar el token
     */
    public static String generateToken(String username, Collection<? extends GrantedAuthority> authorities){
        try {
            Claims claims = Jwts.claims()
                    .add(JwtAuthenticationFilter.AUTHORITIES, new ObjectMapper().writeValueAsString(authorities))
                    .add(JwtAuthenticationFilter.USERNAME, username)
                    .build();

            return Jwts.builder()
                    .subject(username)
                    .claims(claims)
                    .expiration(new Date(System.currentTimeMillis() + 3600000))
                    .issuedAt(new Date())
                    .signWith(SECRET_KEY)
                    .compact();

        } catch (Exception e) {
            throw new RuntimeException("Al generar el token JWT",e);
        }
    }
}
