package com.carpool.carpool.service.notification;

import com.carpool.carpool.model.user.User;

public interface INotificationService {
    /**
     * Envía una notificación al usuario especificado.
     *
     * <p>
     * Dependiendo de la disponibilidad de tokens push activos, la notificación puede ser:
     * <ul>
     *     <li>Push notification a dispositivos vinculados.</li>
     *     <li>Email al correo registrado del usuario si no hay dispositivos push activos.</li>
     * </ul>
     * </p>
     *
     * @param user  el usuario al que se desea notificar. Debe contener al menos el email y la relación con tokens push.
     * @param title el título de la notificación o correo.
     * @param body  el cuerpo del mensaje de la notificación o correo.
     */
    void notifyUser(User user, String title, String body);
}
