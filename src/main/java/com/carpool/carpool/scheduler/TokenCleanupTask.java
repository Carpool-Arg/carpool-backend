package com.carpool.carpool.scheduler;

import com.carpool.carpool.enums.token.TokenStateEnum;
import com.carpool.carpool.enums.token.TokenTypeEnum;
import com.carpool.carpool.model.user.UserToken;
import com.carpool.carpool.repository.user.token.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenCleanupTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenCleanupTask.class);

    private final UserTokenRepository userTokenRepository;

    /**
     * CronTask que se ejecuta todos los dias a las 4:00 AM (horario de baja carga) y que obtiene aquellas solicitudes para activar la cuenta cuyo token se encuentre expirado, es decir,
     * que el token tenga estado {@code PENDING}, que sea del tipo {@code ACTIVATION} y que la fecha y hora actual sea mayor a {@code expires_at}, de este modo se actualiza
     * el estado de cada token a {@code EXPIRED}.
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void deleteExpiredActivationTokens() {

        LOGGER.info("Inicio de crontask para validar tokens");
        LocalDateTime now = LocalDateTime.now();
        int count = 0;

        List<UserToken> expiredTokens = userTokenRepository.findByTypeAndExpiresAtBeforeAndState(TokenTypeEnum.ACTIVATION, now, TokenStateEnum.PENDING);

        for (UserToken token : expiredTokens) {
            token.setState(TokenStateEnum.EXPIRED);
            userTokenRepository.save(token);
            count++;
        }
        LOGGER.info("Cantidad de tokens invalidados: {}", count);
    }
}
