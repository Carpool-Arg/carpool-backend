package com.carpool.carpool.utils;

import com.carpool.carpool.repository.user.token.UserTokenRepository;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.UUID;

public class TokenUtils {

    /**
     * Metodo encargado de generar un token único.
     * @return token del tipo {@link String}
     */
    public static String generateToken(UserTokenRepository userTokenRepository){
        String token;
        do {
            token = UUID.randomUUID().toString().replace("-", "")
                    + RandomStringUtils.randomAlphanumeric(32);
        } while (userTokenRepository.findByToken(token).isPresent());
        return token;
    }
}
