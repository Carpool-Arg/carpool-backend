package com.carpool.carpool.enums.notificationEvent;

/**
 * Clase que contiene las diferentes notificaciones que la aplicación puede realizar
 * <p>
 *     {@code RESERVATION_CREATED} Notificación para cuando se crea una reserva
 * </p>
 */
public enum NotificationEventEnum {
    RESERVATION_CREATED,
    RESERVATION_ACCEPTED,
    RESERVATION_REJECTED,
    TRIP_CLOSED_AUTOMATICALLY
}
