package com.carpool.carpool.service.auth.blacklist;

import com.carpool.carpool.response.Response;
import com.carpool.carpool.enums.response.ResponseStateEnum;
import com.carpool.carpool.security.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Clase encargada de dar de baja un token, guardandolo en una blacklist en redis
 */
@Service
public class AuthBlacklistImplementation implements IAuthBlacklistService{

    //Template para operar con Redis usando claves y valores tipo String.
    private final StringRedisTemplate redisTemplate;

    //Prefijo que se utiliza en las claves de Redis para los tokens en blacklist.
    private final String TOKEN_BLACKLIST_PREFIX = "blacklisted:";

    @Autowired
    private JwtUtils jwtUtils;

    public AuthBlacklistImplementation(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Agrega un token JWT a la blacklist con un tiempo de expiración igual
     * al tiempo restante de vida del token.
     *
     * @param token token JWT completo (con prefijo "Bearer ")
     * @return response respuesta con mensaje y estado de la operación
     */
    @Override
    public Response<Void> blacklistToken(String token) {
        try {
            //Obtenemos el token
            String jwtToken = token.replace("Bearer ", "");

            //Calcular el tiempo de expiracion de dicho token
            long expirationTime = jwtUtils.getTokenExpiration(jwtToken);

            //Cargar el token en redis, con el prefijo especificado  y el valor true (sirve para saber que la clave existe)
            redisTemplate.opsForValue().set(TOKEN_BLACKLIST_PREFIX + jwtToken, "true", expirationTime, TimeUnit.SECONDS);

            return new Response<>(List.of("Sesión cerrada correctamente"), ResponseStateEnum.OK);
        } catch (RedisConnectionFailureException e) {
            throw new RedisConnectionFailureException(e.getMessage());
        }
    }

    /**
     * Verifica si un token JWT está en la blacklist.
     *
     * @param token token JWT limpio (sin el prefijo "Bearer ")
     * @return true si el token está en blacklist, false si no
     */
    @Override
    public boolean isTokenBlacklisted(String token) {
        try {
            return redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token);
        } catch (RedisConnectionFailureException e) {
            throw new RedisConnectionFailureException(e.getMessage());
        }
    }
}
