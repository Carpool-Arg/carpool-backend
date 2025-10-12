package com.carpool.carpool.service.firebase.notification;

import com.carpool.carpool.model.user.User;

public interface IFirebaseNotificationService {    /**
     * Envía una notificación push a todos los dispositivos del usuario.
     *
     * @param user  Usuario destinatario.
     * @param title Título de la notificación.
     * @param body  Mensaje de la notificación.
     */
    void sendPushNotification(User user, String title, String body);

    /**
     * Envía una notificación push silenciosa a todos los dispositivos del usuario.
     *
     * <p>
     * Este método usa la API v1 de Firebase (a través del SDK de Firebase Admin) para
     * enviar un mensaje "data-only" al token proporcionado. El mensaje no se muestra en la UI
     * del dispositivo, y su único propósito es verificar si el token es válido y puede recibir
     * notificaciones push.
     * </p>
     * @param String  token que se almacenara en la base de datos.
     * @return  {@code true} si es posible enviar la notificacion; {@code false} si el token no es válido
     */
    boolean sendSilentPush(String token);


}
