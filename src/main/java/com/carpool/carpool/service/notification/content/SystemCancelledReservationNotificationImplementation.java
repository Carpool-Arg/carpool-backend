package com.carpool.carpool.service.notification.content;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.model.reservation.Reservation;
import org.springframework.stereotype.Component;

import static com.carpool.carpool.utils.EmailMessageUtils.*;

@Component
public class SystemCancelledReservationNotificationImplementation implements INotificationContentService<Reservation> {

    @Override
    public NotificationEventEnum getEvent() {
        return NotificationEventEnum.RESERVATION_CANCELLED_BY_SYSTEM;
    }

    @Override
    public DispatchPolicyEnum getPolicy() {
        // Siguiendo tu lógica de negocio para cancelaciones automáticas
        return DispatchPolicyEnum.PUSH_THEN_EMAIL;
    }

    @Override
    public NotificationPayloadDTO build(Reservation reservation) {
        String passengerName = reservation.getUser().getName();
        String origin = reservation.getStartCity().getCity().getName();
        String destination = reservation.getDestinationCity().getCity().getName();

        return NotificationPayloadDTO.builder()
                .pushTitle("Reserva cancelada")
                .pushBody(String.format("%s, tu solicitud para el viaje %s -> %s fue cancelada por cierre del viaje.",
                        passengerName, origin, destination))
                .emailSubject(SUBJECT_EMAIL_RESERVATION_CANCELLED_SYSTEM)
                .emailTitle(TITLE_GREETING.replace("{name}", passengerName))
                .emailMessage(MESSAGE_RESERVATION_CANCELLED_SYSTEM
                        .replace("{origin}", origin)
                        .replace("{destination}", destination))
                .emailOptionalMessage(null)
                .emailButtonUrl(null)
                .emailButtonText(null)
                .emailMessageFooter(MESSAGE_FOOTER_RESERVATION_CANCELLED_SYSTEM)
                .build();
    }
}