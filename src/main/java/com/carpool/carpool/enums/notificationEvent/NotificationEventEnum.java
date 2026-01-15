package com.carpool.carpool.enums.notificationEvent;

/**
 * Clase que contiene las diferentes notificaciones que la aplicación puede realizar
 * <p>
 *     {@code RESERVATION_CREATED} Notificación para cuando se crea una reserva
 *     {@code RESERVATION_ACCEPTED} Notificación para cuando se acepta una reserva
 *     {@code RESERVATION_REJECTED} Notificación para cuando se rechaza una reserva
 *     {@code RESERVATION_REJECTED} Notificación para cuando se finaliza el viaje y la reserva se tiene que pagar
 *
 * </p>
 */
public enum NotificationEventEnum {
    RESERVATION_CREATED,
    RESERVATION_ACCEPTED,
    RESERVATION_REJECTED,
    RESERVATION_UNPAID
}
