package com.carpool.carpool.service.notification.content;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.model.reservation.Reservation;
import org.springframework.stereotype.Component;

import static com.carpool.carpool.utils.EmailMessageUtils.*;
import static com.carpool.carpool.utils.EmailMessageUtils.MESSAGE_FOOTER_RESERVATION_REJECTED;

@Component
public class RejectReservationNotificationImplementation implements INotificationContentService<Reservation> {

    @Override
    public NotificationEventEnum getEvent() {
        return NotificationEventEnum.RESERVATION_REJECTED;
    }

    @Override
    public DispatchPolicyEnum getPolicy() {
        return DispatchPolicyEnum.PUSH_THEN_EMAIL;
    }

    @Override
    public NotificationPayloadDTO build(Reservation reservation) {
        String passengerName = reservation.getUser().getName();
        return NotificationPayloadDTO.builder()
                .pushTitle("Reserva rechazada")
                .pushBody(String.format("%s, tu solicitud de reserva para el viaje de %s -> %s, fue rechazada.",
                        passengerName, reservation.getStartCity().getCity().getName(), reservation.getDestinationCity().getCity().getName()))
                .emailSubject(SUBJECT_EMAIL_RESERVATION_REJECTED)
                .emailTitle(TITLE_RESERVATION_REJECTED.replace("{name}", reservation.getUser().getName()))
                .emailMessage(MESSAGE_RESERVATION_REJECTED
                        .replace("{origin}", reservation.getStartCity().getCity().getName())
                        .replace("{destination}", reservation.getDestinationCity().getCity().getName())
                        .replace("{driverName}", reservation.getTrip().getVehicle().getDriver().getUser().getName()))
                .emailOptionalMessage(null)
                .emailButtonUrl(null)
                .emailButtonText(null)
                .emailMessageFooter(MESSAGE_FOOTER_RESERVATION_REJECTED)
                .build();
    }
}