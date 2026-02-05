package com.carpool.carpool.service.notification.content;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.model.trip.tripStop.TripStop;
import org.springframework.stereotype.Component;

import java.util.Comparator;

import static com.carpool.carpool.utils.EmailMessageUtils.*;

@Component
public class TripCancelledNotificationImplementation implements INotificationContentService<Trip>  {
    @Override
    public NotificationEventEnum getEvent() { return NotificationEventEnum.TRIP_CANCELLED;}

    @Override
    public DispatchPolicyEnum getPolicy() {return DispatchPolicyEnum.EMAIL_ONLY;}

    @Override
    public NotificationPayloadDTO build(Trip trip) {
        String driver = trip.getVehicle().getDriver().getUser().getName();

        String origin = trip.getTripStops().stream()
                .min(Comparator.comparing(TripStop::getStopOrder))
                .map(ts -> ts.getCity().getName())
                .orElse("desconocido");

        String destination = trip.getTripStops().stream()
                .max(Comparator.comparing(TripStop::getStopOrder))
                .map(ts -> ts.getCity().getName())
                .orElse("desconocido");

        return NotificationPayloadDTO.builder()
                .emailSubject(SUBJECT_TRIP_CANCELLED)
                .emailTitle(TITLE_TRIP_CANCELLED)
                .emailMessage(
                        MESSAGE_TRIP_CANCELLED
                                .replace(
                                        "{origin}",
                                        origin
                                )
                                .replace(
                                        "{destination}",
                                        destination
                                )
                                .replace(
                                        "{driver}",
                                        driver
                                )
                                .replace(
                                        "{reason}",
                                        trip.getCancellationReason()
                                )
                )
                .emailMessageFooter(FOOTER_TRIP_CANCELLED)
                .build();
    }
}
