package com.carpool.carpool.service.notification.dispatcher;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.service.notification.dispatch.INotificationDispatchPolicyService;
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
public class NotificationDispatcherImplementation implements INotificationDispatcherService {
    // Mapa que asocia cada política (WS, PUSH, EMAIL) con su implementación real
    private final Map<DispatchPolicyEnum, INotificationDispatchPolicyService> policyStrategies;

    @Autowired
    public NotificationDispatcherImplementation(List<INotificationDispatchPolicyService> strategies) {
        // Transformamos la lista de servicios inyectados en un mapa para acceso O(1)
        this.policyStrategies = strategies.stream()
                .collect(Collectors.toMap(INotificationDispatchPolicyService::getPolicy, Function.identity()));
    }

    @Override
    public boolean dispatch(User user, NotificationPayloadDTO payload, DispatchPolicyEnum policy) {
        // 1. Buscamos el servicio de envío correspondiente (ej. el de WebSocket)
        INotificationDispatchPolicyService strategy = policyStrategies.get(policy);

        if (strategy == null) {
            log.error("No se encontró una implementación para la política: {}", policy);
            return false;
        }

        // 2. Intentamos el envío y retornamos el resultado (true/false)
        // Esto es vital para saber si el fallback debe activarse
        return strategy.execute(user, payload);
    }
}
