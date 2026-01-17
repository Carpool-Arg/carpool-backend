package com.carpool.carpool.service.notification.content;

import java.util.Comparator;

import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.model.trip.tripStop.TripStop;

import static com.carpool.carpool.utils.EmailMessageUtils.*;

@Component
public class TripClosedAutomaticallyNotificationImplementation implements INotificationContentService<Trip> {

    @Override
    public NotificationEventEnum getEvent() {
       return NotificationEventEnum.TRIP_CLOSED_AUTOMATICALLY;
    }   

    @Override
    public DispatchPolicyEnum getPolicy() {
        return DispatchPolicyEnum.EMAIL_ONLY;
    }

    @Override
    public NotificationPayloadDTO build(Trip context) {
        
        String driverName = context.getVehicle().getDriver().getUser().getName();
        
        String origin = context.getTripStops().stream()
            .min(Comparator.comparing(TripStop::getStopOrder))
            .map(ts -> ts.getCity().getName())
            .orElse("desconocido");

        String destination = context.getTripStops().stream()
                .max(Comparator.comparing(TripStop::getStopOrder))
                .map(ts -> ts.getCity().getName())
                .orElse("desconocido");

        return NotificationPayloadDTO.builder()
                .pushTitle("Viaje cerrado")
                .pushBody(String.format("El viaje conducido por %s hacia %s ha sido cerrado automáticamente.", driverName, destination))
                .emailSubject(SUBJECT_TRIP_CLOSED_AUTOMATICALLY)
                .emailTitle(TITLE_TRIP_CLOSED_AUTOMATICALLY.replace("{name}", driverName))
                .emailMessage(MESSAGE_TRIP_CLOSED_AUTOMATICALLY.replace("{origin}", origin).replace("{destination}", destination))
                .emailButtonText(null)
                .emailButtonUrl(null)
                .emailMessageFooter(MESSAGE_FOOTER_TRIP_CLOSED_AUTOMATICALLY)
                .build();
    }
    
}
