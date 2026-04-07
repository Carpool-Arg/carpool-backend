package com.carpool.carpool.service.notification.content;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.model.reservation.Reservation;
import org.springframework.stereotype.Component;

import static com.carpool.carpool.utils.EmailMessageUtils.*;

@Component
public class CancelReservationNotificationImplementation implements INotificationContentService<Reservation>{
    @Override
    public NotificationEventEnum getEvent() {
        return NotificationEventEnum.RESERVATION_CANCELLED_BY_PASSENGER;
    }

    @Override
    public DispatchPolicyEnum getPolicy() {
        return DispatchPolicyEnum.EMAIL_ONLY;
    }

    @Override
    public NotificationPayloadDTO build(Reservation reservation) {
        String passengerName = reservation.getUser().getName();
        String driverName = reservation.getTrip().getVehicle().getDriver().getUser().getName();
        String origin = reservation.getStartCity().getCity().getName();
        String destination = reservation.getDestinationCity().getCity().getName();

        return NotificationPayloadDTO.builder()
                .emailSubject(SUBJECT_RESERVATION_CANCELLED_BY_PASSENGER)
                .emailTitle(TITLE_GREETING.replace("{name}", driverName))
                .emailMessage(MESSAGE_RESERVATION_CANCELLED_BY_PASSENGER
                        .replace("{passenger}", passengerName)
                        .replace("{origin}", origin)
                        .replace("{destination}", destination))
                .emailOptionalMessage(null)
                .emailButtonUrl(null)
                .emailButtonText(null)
                .emailMessageFooter(MESSAGE_FOOTER_RESERVATION_CANCELLED_BY_PASSENGER)
                .build();
    }
}
