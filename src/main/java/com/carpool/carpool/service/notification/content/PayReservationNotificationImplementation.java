package com.carpool.carpool.service.notification.content;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.model.reservation.Reservation;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PayReservationNotificationImplementation implements INotificationContentService<Reservation>{
    @Override
    public NotificationEventEnum getEvent() { return NotificationEventEnum.RESERVATION_UNPAID; }

    @Override
    public DispatchPolicyEnum getPolicy() { return DispatchPolicyEnum.WEB_SOCKET; }

    @Override
    public NotificationPayloadDTO build(Reservation reservation) {
        String passengerName = reservation.getUser().getName();
        return NotificationPayloadDTO.builder()
                .type("PAYMENT_PENDING")
                .pushTitle("¡Pagá tu viaje!")
                .pushBody(passengerName + " tenés un pago pendiente")
                .data(Map.of(
                        "reservationId", reservation.getId(),
                        "total", reservation.getTotal(),
                        "currency", "ARS"
                ))
                .build();
    }
}
