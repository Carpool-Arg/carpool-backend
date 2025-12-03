package com.carpool.carpool.service.notification.content;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.model.reservation.Reservation;
import org.springframework.stereotype.Component;

import static com.carpool.carpool.utils.EmailMessageUtils.*;

@Component
public class AcceptReservationNotificationImplementation implements INotificationContentService<Reservation> {

    @Override
    public NotificationEventEnum getEvent() {
        return NotificationEventEnum.RESERVATION_ACCEPTED;
    }

    @Override
    public DispatchPolicyEnum getPolicy() {
        return DispatchPolicyEnum.PUSH_THEN_EMAIL;
    }

    @Override
    public NotificationPayloadDTO build(Reservation reservation) {
        String passengerName = reservation.getUser().getName();
        return NotificationPayloadDTO.builder()
                .pushTitle("¡Reserva aceptada!")
                .pushBody(String.format("%s, tenés tu lugar asegurado para el viaje de %s > %s",
                        passengerName, reservation.getStartCity().getCity().getName(), reservation.getDestinationCity().getCity().getName()))
                .emailSubject(SUBJECT_EMAIL_RESERVATION_ACCEPTED)
                .emailTitle(TITLE_RESERVATION_ACCEPTED.replace("{name}", reservation.getUser().getName()))
                .emailMessage(MESSAGE_RESERVATION_ACCEPTED
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
