package com.carpool.carpool.service.notification.dispatcher;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.service.notification.dispatch.INotificationDispatchPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationDispatcherImplementation implements INotificationDispatcherService {
    private final Map<DispatchPolicyEnum, INotificationDispatchPolicyService> policyStrategies;

    @Autowired
    public  NotificationDispatcherImplementation(List<INotificationDispatchPolicyService> strategies) {
        // Inyección de todas las estrategias de canal en un mapa para acceso
        this.policyStrategies = strategies.stream()
                .collect(Collectors.toMap(INotificationDispatchPolicyService::getPolicy, Function.identity()));
    }

    @Override
    public void dispatch(User user, NotificationPayloadDTO payload, DispatchPolicyEnum policy) {
        INotificationDispatchPolicyService strategy = policyStrategies.get(policy);
        if (strategy == null) {
            throw new UnsupportedOperationException("Política de despacho no soportada: " + policy);
        }

        // Delega a la estrategia de canal correcta
        strategy.execute(user, payload);
    }
}
