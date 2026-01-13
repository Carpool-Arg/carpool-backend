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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PushThenEmailPolicyImplementation implements INotificationDispatchPolicyService{
    private final UserTokenRepository tokenRepository;
    private final IFirebaseNotificationService firebaseService;
    private final IEmailService emailService;
    private static final Logger logger = LoggerFactory.getLogger(PushThenEmailPolicyImplementation.class);

    @Override
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
            );
        }
    }

    @Override
    public DispatchPolicyEnum getPolicy() { return DispatchPolicyEnum.PUSH_THEN_EMAIL; }
}
