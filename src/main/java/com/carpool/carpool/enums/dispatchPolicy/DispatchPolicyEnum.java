package com.carpool.carpool.enums.dispatchPolicy;

/**
 * Clase que contiene las diferentes políticas de despacho para las notificaciones que manejamos en la aplicación
 * <p>
 *     {@code PUSH_THEN_EMAIL} Envía la notificación PUSH y si el usuario no tiene el dispositivo registrado, se le envía un MAIL.
 *     {@code EMAIL_ONLY} Envía solamente EMAILS
 *     {@code PUSH_THEN_EMAIL} Envía solamente notificaciones PUSH
 *     {@code PUSH_THEN_EMAIL} Envía ambos, tanto notificaciones PUSH como EMAILS
 * </p>
 */
public enum DispatchPolicyEnum{
    PUSH_THEN_EMAIL,
    EMAIL_ONLY,
    PUSH_ONLY,
    PUSH_AND_EMAIL,
    WEB_SOCKET
}
