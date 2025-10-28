package com.carpool.carpool.service.notification;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.service.notification.content.INotificationContentService;
import com.carpool.carpool.service.notification.dispatcher.INotificationDispatcherService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NotificationImplementation implements INotificationService {
    private final INotificationDispatcherService dispatcher;
    private final Map<NotificationEventEnum, INotificationContentService> contentStrategies;

    public NotificationImplementation(
            INotificationDispatcherService dispatcher,
            List<INotificationContentService> strategies
    ) {
        this.dispatcher = dispatcher;
        this.contentStrategies = strategies.stream()
                .collect(Collectors.toMap(INotificationContentService::getEvent, Function.identity()));
    }

    @Override
    public <T> void send(User userToNotify, NotificationEventEnum event, T context) {
        // 1. Encontrar la estrategia
        @SuppressWarnings("unchecked")
        INotificationContentService<T> contentStrategy =
                (INotificationContentService<T>) contentStrategies.get(event);

        if (contentStrategy == null) {
            throw new UnsupportedOperationException("Estrategia de contenido no encontrada: " + event);
        }

        // 2. Construir el PAYLOAD
        NotificationPayloadDTO payload = contentStrategy.build(context);

        // 3. Obtener la POLÍTICA de canal
        DispatchPolicyEnum policy = contentStrategy.getPolicy();

        // 4. Enviar al DESPACHADOR de canal
        dispatcher.dispatch(userToNotify, payload, policy);
    }
}
