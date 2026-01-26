package com.carpool.carpool.service.notification.dispatch;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.model.user.User;

public interface INotificationDispatchPolicyService {
    /**
     * Ejecuta la lógica de envío de la notificación a través de un canal específico.
     * <p>
     * Dependiendo la estrategia,  puede realizar lo siguiente:
     *     <ul>
     *         <li>Enviar notificaciones PUSH</li>
     *         <li>Enviar MAIL</li>
     *         <li>Enviar MAIL, si el destintatario NO tiene un dispositivo con permisos para notifiaciones PUSH</li>
     *         <li>Enviar notificaciones PUSH y MAILS</li>
     *     </ul>
     *
     * @param user    El usuario que recibirá la notificación.
     * @param payload El payload estandarizado que contiene el título, cuerpo y metadatos de la notificación a enviar.
     * @return {@code boolean} resultado sobre si se ejecutó bien o no
     *
     */
    boolean execute(User user, NotificationPayloadDTO payload);

    /**
     * Devuelve la política que esta implementación maneja.
     * <p>
     * Este valor se utiliza como clave dentro del {@link INotificationDispatcherService} para seleccionar la estrategia de envío correcta.
     *
     * @return El {@link DispatchPolicyEnum} asociado a este servicio de despacho.
     */
    DispatchPolicyEnum getPolicy();
}
