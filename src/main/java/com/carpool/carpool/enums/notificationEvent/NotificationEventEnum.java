package com.carpool.carpool.enums.notificationEvent;

/**
 * Clase que contiene las diferentes notificaciones que la aplicación puede realizar
 * <p>
 *     {@code RESERVATION_CREATED} Notificación para cuando se crea una reserva
 *     {@code TRIP_CLOSED_AUTOMATICALLY} Notificación para cuando un viaje se cierra automáticamente
 *     {@code TRIP_CANCELLED_BY_SYSTEM} Notificación sobre la cancelación de un viaje.
 *     {@code TRIP_STARTED} Notificación sobre el inicio de un viaje.
 *     {@code RESERVATION_ACCEPTED} Notificación para cuando se acepta una reserva
 *     {@code RESERVATION_REJECTED} Notificación para cuando se rechaza una reserva
 *     {@code RESERVATION_REJECTED} Notificación para cuando se finaliza el viaje y la reserva se tiene que pagar
 *     {@code TRIP_FULL} Notificacion para cuando se cierra el viaje por cupo lleno
 *     {@code TRIP_CANCELLED} Notificación para cuando el viaje es cancelado manualmente. <br>
 *     {@code LICENSE_REJECTED} Notificación para cuando una licencia es rechazada. <br>
 *     {@code LICENSE_APPROVED} Notificación para cuando una licencia es aprobada. <br>
 * </p>
 */
public enum NotificationEventEnum {
    RESERVATION_CREATED,
    RESERVATION_ACCEPTED,
    RESERVATION_REJECTED,
    TRIP_CLOSED_AUTOMATICALLY,
    TRIP_CANCELLED_BY_SYSTEM,
    TRIP_STARTED,
    RESERVATION_UNPAID,
    RESERVATION_PAID,
    TRIP_FULL,
    TRIP_CANCELLED,
    LICENSE_REJECTED,
    LICENSE_APPROVED
}