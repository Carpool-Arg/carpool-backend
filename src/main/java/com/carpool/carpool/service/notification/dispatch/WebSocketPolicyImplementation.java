package com.carpool.carpool.service.notification.dispatch;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketPolicyImplementation implements  INotificationDispatchPolicyService{
    private final SimpMessagingTemplate messagingTemplate;

    private final SimpUserRegistry userRegistry;

    @Override
    public void execute(User user, NotificationPayloadDTO payload) {
        String username = user.getUsername();
        log.info("Enviando notificacion WS");
        log.info("Usuario: '{}'", username);

        // Verificar sesiones activas
        SimpUser simpUser = userRegistry.getUser(username);

        if (simpUser == null) {
            log.error("ERROR - Usuario '{}' NO está conectado al WS!", username);
            return;
        }

        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/notification",
                payload
        );

        log.info("Notificación enviada mediante WS");
    }

    @Override
    public DispatchPolicyEnum getPolicy() {return DispatchPolicyEnum.WEB_SOCKET;}
}
