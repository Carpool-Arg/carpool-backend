package com.carpool.carpool.service.notification;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.service.notification.content.INotificationContentService;
import com.carpool.carpool.service.notification.dispatcher.INotificationDispatcherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationImplementation implements INotificationService {
    private final INotificationDispatcherService dispatcher;
    private final Map<NotificationEventEnum, INotificationContentService> contentStrategies;

    @Autowired
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
        // 1. Buscamos qué notificación hay que armar (ej. PagoPendiente)
        @SuppressWarnings("unchecked")
        INotificationContentService<T> contentStrategy = (INotificationContentService<T>) contentStrategies.get(event);

        if (contentStrategy == null) {
            throw new UnsupportedOperationException("Evento no soportado: " + event);
        }

        // 2. Construimos el mensaje (Payload) usando la entidad de contexto (ej. Reservation)
        NotificationPayloadDTO payload = contentStrategy.build(context);

        // 3. Obtenemos la LISTA de canales permitidos para esta notificación específica
        // Gracias al 'default' en la interfaz, si no definiste fallback, traerá solo una política.
        List<DispatchPolicyEnum> strategyList = contentStrategy.getDispatchStrategy();

        // 4. FLUJO DE FALLBACK: Recorremos los canales en orden de prioridad
        for (DispatchPolicyEnum policy : strategyList) {
            log.info("Intentando enviar notificación vía: {}", policy);

            // Intentamos despachar
            boolean isSent = dispatcher.dispatch(userToNotify, payload, policy);

            if (isSent) {
                // Si el envío fue exitoso (ej. el usuario estaba en WS),
                // cortamos el loop para no enviar la misma notificación por Push o Mail.
                log.info("Notificación entregada con éxito por {}", policy);
                return;
            }

            // Si llega aquí, es porque isSent fue false (ej. WS desconectado).
            // El 'for' continúa automáticamente con el siguiente canal de la lista.
            log.warn("Canal {} fallido. Buscando siguiente opción...", policy);
        }

        log.error("La notificación para el evento {} no pudo ser entregada por ningún canal.", event);
    }
}