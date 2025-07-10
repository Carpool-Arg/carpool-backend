package com.carpool.carpool.security.config;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;

/**
 * Clase que contiene información para la configuración del JWT.
 */
public class TokenJwtConfig {
    public static final SecretKey SECRET_KEY_ACCESS = Jwts.SIG.HS256.key().build();
    public static final SecretKey SECRET_KEY_REFRESH = Jwts.SIG.HS256.key().build();
    public static final String PREFIX_TOKEN = "Bearer ";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String CONTENT_TYPE = "application/json";
}
