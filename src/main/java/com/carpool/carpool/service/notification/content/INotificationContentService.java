package com.carpool.carpool.service.notification.content;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;

import java.util.List;

public interface INotificationContentService<T>{
    /**
     * Devuelve el evento de negocio específico que maneja la implementación.
     * <p>
     * Este valor se utiliza como clave para seleccionar
     * el servicio de contenido correcto en tiempo de ejecución
     * basado en el evento disparado.
     *
     * @return El {@link NotificationEventEnum} asociado a este servicio de contenido.
     */
    NotificationEventEnum getEvent();

    /**
     * Retorna la política que esta notificación utiliza.
     * <p>
     *     Puede ser:
     *     <ul>
     *         <li>PUSH_THEN_EMAIL</li>
     *         <li>EMAIL_ONLY</li>
     *         <li>PUSH_ONLY</li>
     *         <li>PUSH_AND_EMAIL</li>
     *     </ul>
     * </p>
     * @return El {@link DispatchPolicyEnum} asociado a esta notificación.
     */
    DispatchPolicyEnum getPolicy();

    /**
     * Define la secuencia o estrategia de despacho, permitiendo mecanismos de fallback.
     * <p>
     * Por defecto, devuelve una lista que contiene únicamente la política definida en {@link #getPolicy()}.
     * Si se requiere un flujo tipo "WebSocket -> Push then Email", esta es la función a sobrescribir
     * en la implementación específica.
     * </p>
     *
     * @return Una lista ordenada de {@link DispatchPolicyEnum} que representa la jerarquía de despacho.
     */
    default List<DispatchPolicyEnum> getDispatchStrategy() {
        return List.of(getPolicy());
    }

    /**
     * Construye el payload de notificación
     * Se encarga de construir el DTO que transportara la información que se debe enviar en la notificación
     * Puede enviar tanto la información para notificaciones PUSH y MAIL.
     * @param context Entidad (p.ej., una entidad {@code Reservation})
     * que contiene la información necesaria para construir el mensaje.
     * @return El {@link NotificationPayloadDTO} asociado a esta notificación.
     */
    NotificationPayloadDTO build(T context);
}
