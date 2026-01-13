package com.carpool.carpool.service.firebase.notification;

import com.carpool.carpool.enums.token.TokenStateEnum;
import com.carpool.carpool.model.user.token.UserToken;
import com.carpool.carpool.repository.user.token.UserTokenRepository;
import com.carpool.carpool.service.notification.dispatch.PushThenEmailPolicyImplementation;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FirebaseNotificationImplementation implements IFirebaseNotificationService{
    private final UserTokenRepository userTokenRepository;
    private static final Logger logger = LoggerFactory.getLogger(FirebaseNotificationImplementation.class);


    @Override
    @Async
    public void sendPushNotification(List<UserToken> tokens, String title, String body) {
        for (UserToken token : tokens) {
            try {
                // Construir la notificación
                Notification notification = Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build();

                // Construir el mensaje
                Message message = Message.builder()
                        .setToken(token.getToken())
                        .setNotification(notification)
                        .build();

                String response = FirebaseMessaging.getInstance().send(message);
                logger.debug(
                        "Firebase notification sent. userTokenId={}, state={}, firebaseMessageId={}",
                        token.getId(),
                        token.getState(),
                        response
                );
            } catch (Exception e) {
                // Si falla el envío, marcar token como EXPIRED
                e.printStackTrace();
                token.setState(TokenStateEnum.EXPIRED);
                userTokenRepository.save(token);
            }
        }
    }

    @Override
    public boolean sendSilentPush(String token) {
        try {
            logger.debug("Sending silent push. token={}", token);

            Message message = Message.builder()
                    .setToken(token)
                    // Mensaje silencioso: no se muestra notificación en la UI
                    .putData("silent", "true")
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);

            logger.debug(
                    "Silent push sent successfully. firebaseMessageId={}",
                    response
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
