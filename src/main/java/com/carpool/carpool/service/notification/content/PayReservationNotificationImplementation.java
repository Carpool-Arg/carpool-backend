package com.carpool.carpool.service.notification.content;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.model.reservation.Reservation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.carpool.carpool.utils.EmailMessageUtils.*;


@Component
public class PayReservationNotificationImplementation implements INotificationContentService<Reservation>{
    @Value("${redirect.pay.reservation}")
    private String urlPayReservation;

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
                .data(Map.of( "total", reservation.getTotal(), "tripId", reservation.getTrip().getId()))
                .emailSubject(SUBJECT_EMAIL_RESERVATION_UNPAID)
                .emailTitle(TITLE_RESERVATION_UNPAID.replace("{name}", reservation.getUser().getName()))
                .emailMessage( MESSAGE_RESERVATION_UNPAID.replace( "{total}",String.valueOf(reservation.getTotal())))
                .emailButtonUrl(urlPayReservation)
                .emailButtonText(BUTTON_RESERVATION_UNPAID)
                .emailMessageFooter(MESSAGE_FOOTER_RESERVATION_UNPAID)
                .build();
    }

    @Override
    public List<DispatchPolicyEnum> getDispatchStrategy() {
        // definir fallback por si falla web socket
        return List.of(
                DispatchPolicyEnum.WEB_SOCKET,
                DispatchPolicyEnum.PUSH_THEN_EMAIL
        );
    }
}
