package com.carpool.carpool.service.notification.content;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.model.reservation.Reservation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.carpool.carpool.utils.EmailMessageUtils.*;

@Component
public class NewReservationNotificationImplementation implements INotificationContentService<Reservation> {
    @Value("${redirect.validate.email}")
    private String urlValidateEmail;

    @Override
    public NotificationEventEnum getEvent() {
        return NotificationEventEnum.RESERVATION_CREATED;
    }

    @Override
    public DispatchPolicyEnum getPolicy() {
        // Esta notificación usará el fallback
        return DispatchPolicyEnum.PUSH_THEN_EMAIL;
    }

    @Override
    public NotificationPayloadDTO build(Reservation reservation) {
        String passengerName = reservation.getUser().getName();
        return NotificationPayloadDTO.builder()
                .pushTitle("¡Nueva Reserva!")
                .pushBody(passengerName + " quiere unirse a tu viaje.")
                .emailSubject(SUBJECT_EMAIL_NEW_RESERVATION)
                .emailTitle(TITLE_NEW_RESERVATION.replace("{name}", reservation.getTrip().getVehicle().getDriver().getUser().getName()))
                .emailMessage(MESSAGE_NEW_RESERVATION.replace("{passengerName}", passengerName))
                .emailOptionalMessage(null)
                .emailButtonUrl(urlValidateEmail)
                .emailButtonText(BUTTON_NEW_RESERVATION)
                .emailMessageFooter(MESSAGE_FOOTER_NEW_RESERVATION)
                .build();
    }
}
