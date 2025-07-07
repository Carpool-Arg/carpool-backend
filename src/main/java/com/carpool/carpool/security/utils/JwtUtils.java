package com.carpool.carpool.security.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import com.carpool.carpool.model.user.User;

import static com.carpool.carpool.security.config.TokenJwtConfig.*;

import java.util.Date;

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

    public String generateToken(String username, int time, Claims claims){
        return Jwts.builder()
                .subject(username)
                .claims(claims)
                .expiration(new Date(System.currentTimeMillis() + time)) // 7 dias de duracion 
                .issuedAt(new Date())
                .signWith(SECRET_KEY)
                .compact();
    }

    public String extractUsername(String token){
        final Claims claims = Jwts.parser()
            .verifyWith(SECRET_KEY)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        return claims.getSubject();
    }

    public boolean isTokenValid(final String token, final User user){
        final String username = extractUsername(token);
        return (username.equals(user.getUsername())) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(final String token){
        return getTokenExpiration(token)<=0;
    }
}
