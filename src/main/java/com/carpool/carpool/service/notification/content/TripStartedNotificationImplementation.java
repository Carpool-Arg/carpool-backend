package com.carpool.carpool.service.notification.content;

import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.model.reservation.Reservation;
import static com.carpool.carpool.utils.EmailMessageUtils.*;

@Component
public class TripStartedNotificationImplementation implements INotificationContentService<Reservation> {
    @Override
    public NotificationEventEnum getEvent() {
        return NotificationEventEnum.TRIP_STARTED;
    }

    @Override
    public DispatchPolicyEnum getPolicy() {
        return DispatchPolicyEnum.PUSH_THEN_EMAIL;
    }

    @Override
    public NotificationPayloadDTO build(Reservation res) {
        String driver = res.getTrip().getVehicle().getDriver().getUser().getName();
        String origin = res.getStartCity().getCity().getName();
        String destination = res.getDestinationCity().getCity().getName();

        return NotificationPayloadDTO.builder()
                .pushTitle(PUSH_TITLE_TRIP_STARTED)
                .pushBody(String.format(PUSH_BODY_TRIP_STARTED, driver, origin))
                .emailSubject(SUBJECT_TRIP_STARTED)
                .emailTitle(String.format(TITLE_TRIP_STARTED, res.getUser().getName()))
                .emailMessage(String.format(MESSAGE_TRIP_STARTED, driver, origin, destination))
                .emailMessageFooter(FOOTER_TRIP_STARTED)
                .build();
    }
}
