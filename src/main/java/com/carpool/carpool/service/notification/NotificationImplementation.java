package com.carpool.carpool.service.notification;

import com.carpool.carpool.enums.token.TokenStateEnum;
import com.carpool.carpool.enums.token.TokenTypeEnum;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.model.user.token.UserToken;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.repository.user.token.UserTokenRepository;
import com.carpool.carpool.service.email.IEmailService;
import com.carpool.carpool.service.firebase.notification.IFirebaseNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationImplementation implements INotificationService {
    private final UserTokenRepository userTokenRepository;
    private final IFirebaseNotificationService firebaseNotificationService;
    private final IEmailService emailService;

    @Override
    public void notifyUser(User user, String title, String body) {
        // Buscar tokens activos
        List<UserToken> tokens = userTokenRepository.findByUserAndTypeAndState(
                user, TokenTypeEnum.PUSH_NOTIFICATION, TokenStateEnum.ACTIVE
        );

        if (!tokens.isEmpty()) {
            // Enviar notificación push a cada token
            for (UserToken token : tokens) {
                firebaseNotificationService.sendPushNotification(user,title,body);
            }
        } else {
            // Si no hay tokens, enviar email
            emailService.sendEmail();
        }
    }
}
