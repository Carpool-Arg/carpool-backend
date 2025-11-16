package com.carpool.carpool.service.notification;

import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.model.user.User;

public interface INotificationService {
    /**
     * Envía una notificación a un usuario basada en un evento de negocio.
     * <p>
     * Este es el método principal del sistema de notificaciones.
     *
     * @param <T>          El tipo genérico del objeto de contexto.
     * @param userToNotify El usuario que debe recibir la notificación.
     * @param event        El {@link NotificationEventEnum} que describe que tipo de notificacion se envía
     * @param context      Entidad (p.ej., la entidad {@code Reservation})
<<<<<<< HEAD
     * que se pasará al {@link INotificationService}
=======
     * que se pasará al {@link INotificationContentService}
>>>>>>> origin/dev
     * para construir el mensaje.
     */
    <T> void send(User userToNotify, NotificationEventEnum event, T context);
}
