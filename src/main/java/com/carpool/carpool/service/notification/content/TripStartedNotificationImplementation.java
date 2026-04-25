package com.carpool.carpool.service.notification.content;

import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.model.reservation.Reservation;
import com.carpool.carpool.model.trip.tripStop.TripStop;

import static com.carpool.carpool.utils.EmailMessageUtils.*;

import java.util.Comparator;
import java.util.List;

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
        List<TripStop> stops = res.getTrip().getTripStops().stream()
                .sorted(Comparator.comparingInt(TripStop::getStopOrder))
                .toList();
        String tripOrigin = stops.get(0).getCity().getName();
        String tripDestination = stops.get(stops.size() - 1).getCity().getName();

        return NotificationPayloadDTO.builder()
                .pushTitle(PUSH_TITLE_TRIP_STARTED)
                .pushBody(String.format(PUSH_BODY_TRIP_STARTED, driver, tripOrigin))
                .emailSubject(SUBJECT_TRIP_STARTED)
                .emailTitle(String.format(TITLE_TRIP_STARTED, res.getUser().getName()))
                .emailMessage(String.format(MESSAGE_TRIP_STARTED, driver, tripOrigin, tripDestination))
                .emailMessageFooter(FOOTER_TRIP_STARTED)
                .build();
    }
}
