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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushThenEmailPolicyImplementation implements INotificationDispatchPolicyService{
    private final UserTokenRepository tokenRepository;
    private final IFirebaseNotificationService firebaseService;
    private final IEmailService emailService;

    @Override
    public boolean execute(User user, NotificationPayloadDTO payload) {
        try {
            List<UserToken> tokens = tokenRepository.findByUserAndTypeAndState(
                    user, TokenTypeEnum.PUSH_NOTIFICATION, TokenStateEnum.ACTIVE
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
