package com.carpool.carpool.service.notification.dispatch;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.enums.token.TokenStateEnum;
import com.carpool.carpool.enums.token.TokenTypeEnum;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.model.user.token.UserToken;
import com.carpool.carpool.repository.user.token.UserTokenRepository;
import com.carpool.carpool.service.email.IEmailService;
import com.carpool.carpool.service.firebase.notification.IFirebaseNotificationService;
import com.carpool.carpool.service.notification.NotificationImplementation;
import lombok.RequiredArgsConstructor;
<<<<<<< HEAD
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
=======
import lombok.extern.slf4j.Slf4j;
>>>>>>> 5921a09fc7fa9cfdc6a103b560add5e267df7b64
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushThenEmailPolicyImplementation implements INotificationDispatchPolicyService{
    private final UserTokenRepository tokenRepository;
    private final IFirebaseNotificationService firebaseService;
    private final IEmailService emailService;
    private static final Logger logger = LoggerFactory.getLogger(PushThenEmailPolicyImplementation.class);

    @Override
<<<<<<< HEAD
    public void execute(User user, NotificationPayloadDTO payload) {
        List<UserToken> tokens = tokenRepository.findByUserAndTypeAndState(
                user, TokenTypeEnum.PUSH_NOTIFICATION, TokenStateEnum.ACTIVE
        );

        logger.info("Policy - Push Then Email. Tokens={}", tokens);

        if (!tokens.isEmpty()) {
            firebaseService.sendPushNotification(tokens, payload.getPushTitle(), payload.getPushBody());
        } else {
            emailService.sendEmail(
                    user.getEmail(),
                    payload.getEmailSubject(),
                    payload.getEmailTitle(),
                    payload.getEmailMessage(),
                    payload.getEmailOptionalMessage(),
                    payload.getEmailButtonUrl(),
                    payload.getEmailButtonText(),
                    payload.getEmailMessageFooter()
=======
    public boolean execute(User user, NotificationPayloadDTO payload) {
        try {
            List<UserToken> tokens = tokenRepository.findByUserAndTypeAndState(
                    user, TokenTypeEnum.PUSH_NOTIFICATION, TokenStateEnum.ACTIVE
>>>>>>> 5921a09fc7fa9cfdc6a103b560add5e267df7b64
            );

            if (!tokens.isEmpty()) {
                // Intentamos Push
                firebaseService.sendPushNotification(tokens, payload.getPushTitle(), payload.getPushBody());
                log.info("Notificación Push enviada exitosamente a {}", user.getUsername());
                return true;
            } else {
                // Si no hay tokens, aplicamos el fallback interno al Email
                log.warn("No hay tokens Push para {}, reintentando por Email dentro de la política", user.getUsername());
                emailService.sendEmail(
                        user.getEmail(),
                        payload.getEmailSubject(),
                        payload.getEmailTitle(),
                        payload.getEmailMessage(),
                        payload.getEmailOptionalMessage(),
                        payload.getEmailButtonUrl(),
                        payload.getEmailButtonText(),
                        payload.getEmailMessageFooter()
                );
                return true;
            }
        } catch (Exception e) {
            log.error("Fallo crítico en política PushThenEmail para {}: {}", user.getUsername(), e.getMessage());
            return false;
        }
    }

    @Override
    public DispatchPolicyEnum getPolicy() { return DispatchPolicyEnum.PUSH_THEN_EMAIL; }
}
