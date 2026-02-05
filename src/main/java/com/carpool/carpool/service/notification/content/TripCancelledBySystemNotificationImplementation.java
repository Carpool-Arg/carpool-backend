package com.carpool.carpool.service.notification.content;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.model.reservation.Reservation;
import static com.carpool.carpool.utils.EmailMessageUtils.*;

import org.springframework.stereotype.Component;

@Component
public class TripCancelledBySystemNotificationImplementation implements INotificationContentService<Reservation> {
    @Override
    public NotificationEventEnum getEvent() {
        return NotificationEventEnum.TRIP_CANCELLED_BY_SYSTEM;
    }

    @Override
    public DispatchPolicyEnum getPolicy() {
        return DispatchPolicyEnum.PUSH_THEN_EMAIL;
    }

    @Override
    public NotificationPayloadDTO build(Reservation res) {
        String destination = res.getDestinationCity().getCity().getName();

        return NotificationPayloadDTO.builder()
                .pushTitle(PUSH_TITLE_TRIP_CANCELLED_BY_SYSTEM)
                .pushBody(String.format(PUSH_BODY_TRIP_CANCELLED_BY_SYSTEM, destination))
                .emailSubject(SUBJECT_TRIP_CANCELLED_BY_SYSTEM)
                .emailTitle(TITLE_TRIP_CANCELLED_BY_SYSTEM)
                .emailMessage(String.format(MESSAGE_TRIP_CANCELLED_BY_SYSTEM, destination))
                .emailMessageFooter(FOOTER_TRIP_CANCELLED_BY_SYSTEM)
                .build();
    }
}