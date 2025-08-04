package com.carpool.carpool.utils;

import com.carpool.carpool.enums.token.TokenStateEnum;
import com.carpool.carpool.enums.token.TokenTypeEnum;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.model.user.UserToken;
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

    /**
     * Metodo encargado de crear un objeto {@link UserToken}
     * @return Objeto {@link UserToken}
     */
    public static UserToken buildUserToken(User user, TokenTypeEnum tokenType,UserTokenRepository userTokenRepository){
        String token = TokenUtils.generateToken(userTokenRepository);
        return UserToken.builder()
                .token(token)
                .type(tokenType)
                .state(TokenStateEnum.PENDING)
                .user(user)
                .build();
    }
}
