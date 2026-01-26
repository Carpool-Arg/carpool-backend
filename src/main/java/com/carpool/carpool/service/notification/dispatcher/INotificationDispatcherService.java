package com.carpool.carpool.service.notification.dispatcher;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.model.user.User;

/**
 * Servicio ruteador encargado de delegar el envío de notificaciones al canal técnico correspondiente.
 * <p>
 * Actúa como un componente intermedio en la arquitectura de notificaciones, abstrayendo
 * al orquestador de los detalles de implementación de cada canal (WS, Push, Email).
 * </p>
 */
public interface INotificationDispatcherService {
    /**
     * Enruta y despacha una notificación al usuario basándose en la política definida.
     *
     * @param user    El usuario destinatario de la notificación.
     * @param payload El payload estandarizado (título, cuerpo, data) a enviar.
     * @param policy  La política que define el canal técnico a utilizar.
     * @return {@code true} si la notificación fue entregada con éxito por el canal;
     * {@code false} si el canal no pudo realizar la entrega (ej. usuario desconectado del WebSocket),
     * permitiendo así disparar mecanismos de fallback.
     */
    boolean dispatch(User user, NotificationPayloadDTO payload, DispatchPolicyEnum policy);
}
