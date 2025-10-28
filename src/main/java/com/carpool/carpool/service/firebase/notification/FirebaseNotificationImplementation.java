package com.carpool.carpool.service.firebase.notification;

import com.carpool.carpool.enums.token.TokenStateEnum;
import com.carpool.carpool.enums.token.TokenTypeEnum;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.model.user.token.UserToken;
import com.carpool.carpool.repository.user.token.UserTokenRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FirebaseNotificationImplementation implements IFirebaseNotificationService{
    private final UserTokenRepository userTokenRepository;

    @Override
    @Async
    public void sendPushNotification(List<UserToken> tokens, String title, String body) {

        for (UserToken token : tokens) {
            try {
                Message message = Message.builder()
                        .setToken(token.getToken())
                        .putData("title", title)
                        .putData("body", body)
                        .build();

                FirebaseMessaging.getInstance().send(message);

            } catch (Exception e) {
                // Si falla el envío, marcar token como EXPIRED
                token.setState(TokenStateEnum.EXPIRED);
                userTokenRepository.save(token);
            }
        }
    }

    @Override
    public boolean sendSilentPush(String token) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    // Mensaje silencioso: no se muestra notificación en la UI
                    .putData("silent", "true")
                    .build();

            FirebaseMessaging.getInstance().send(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
