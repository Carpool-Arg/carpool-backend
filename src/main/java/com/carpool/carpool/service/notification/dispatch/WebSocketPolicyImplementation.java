package com.carpool.carpool.service.notification.dispatch;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@RequiredArgsConstructor
@Slf4j
public class WebSocketPolicyImplementation implements  INotificationDispatchPolicyService{
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void execute(User user, NotificationPayloadDTO payload) {
        log.info("Enviando notificacion WS al usuario = {} con payload= {}",user.getId(), payload.getPushBody());
        messagingTemplate.convertAndSendToUser(
                String.valueOf(user.getId()),
                "/notification", //TODO Ver que nombre le pongo al endpoint del frontend
                payload
        );
    }

    @Override
    public DispatchPolicyEnum getPolicy() {return DispatchPolicyEnum.WEB_SOCKET;}
}
