package com.carpool.carpool.service.notification.content;

import static com.carpool.carpool.utils.EmailMessageUtils.*;

import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.model.reservation.Reservation;


@Component
public class AcceptReservationWhitOlverlapImplementation extends AcceptReservationNotificationImplementation {

    @Override
    public NotificationEventEnum getEvent() {
        return NotificationEventEnum.RESERVATION_ACCEPTED_WITH_OVERLAP;
    }

   @Override
    public NotificationPayloadDTO build(Reservation reservation) {
        String passengerName = reservation.getUser().getName();
        return NotificationPayloadDTO.builder()
                .pushTitle("¡Reserva aceptada!")
                .pushBody(String.format("%s, tenés tu lugar asegurado para el viaje de %s > %s",
                        passengerName, reservation.getStartCity().getCity().getName(), reservation.getDestinationCity().getCity().getName()))
                .emailSubject(SUBJECT_EMAIL_RESERVATION_ACCEPTED)
                .emailTitle(TITLE_GREETING.replace("{name}", reservation.getUser().getName()))
                .emailMessage(MESSAGE_RESERVATION_ACCEPTED_WITH_OLVERLAP
                        .replace("{origin}", reservation.getStartCity().getCity().getName())
                        .replace("{destination}", reservation.getDestinationCity().getCity().getName())
                        .replace("{driverName}", reservation.getTrip().getVehicle().getDriver().getUser().getName()))
                .emailOptionalMessage(null)
                .emailButtonUrl(null)
                .emailButtonText(null)
                .emailMessageFooter(MESSAGE_FOOTER_RESERVATION_ACCEPTED)
                .build();
    }
}