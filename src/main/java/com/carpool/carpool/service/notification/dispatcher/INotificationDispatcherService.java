package com.carpool.carpool.service.notification.dispatcher;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.model.user.User;

public interface INotificationDispatcherService {
    /**
     * Enruta y despacha una notificación al usuario basándose en la política definida.
     * <p>
     * La implementación de este método buscará en el registro de servicios
     * de despacho ({@link INotificationDispatchPolicyService}) aquellos que
     * coincidan con la {@code policy} y llamará a sus métodos {@code execute}.
     *
     * @param user    El usuario destinatario de la notificación.
     * @param payload El payload estandarizado (título, cuerpo, etc.) a enviar.
     * @param policy  La política que define qué canal(es) utilizar (p.ej., EMAIL, PUSH).
     */
    void dispatch(User user, NotificationPayloadDTO payload, DispatchPolicyEnum policy);
}
