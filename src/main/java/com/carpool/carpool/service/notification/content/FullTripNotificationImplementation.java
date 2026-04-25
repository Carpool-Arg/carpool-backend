package com.carpool.carpool.service.notification.content;

import static com.carpool.carpool.utils.EmailMessageUtils.MESSAGE_FOOTER_TRIP_FULL;
import static com.carpool.carpool.utils.EmailMessageUtils.MESSAGE_TRIP_FULL;
import static com.carpool.carpool.utils.EmailMessageUtils.SUBJECT_TRIP_FULL;
import static com.carpool.carpool.utils.EmailMessageUtils.TITLE_GREETING;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.model.trip.tripStop.TripStop;

@Component
public class FullTripNotificationImplementation implements INotificationContentService<Trip>{

    @Override
    public NotificationEventEnum getEvent() {return NotificationEventEnum.TRIP_FULL;}

    @Override
    public DispatchPolicyEnum getPolicy() {return DispatchPolicyEnum.EMAIL_ONLY;}

    @Override
    public NotificationPayloadDTO build(Trip context) {
        final String driverName = context.getVehicle().getDriver().getUser().getName();
        final List<TripStop> tripStops = context.getTripStops();
        final String origin = tripStops.stream()
                .min(Comparator.comparing(TripStop::getStopOrder))
                .map(tripStop -> tripStop.getCity().getName())
                .orElse("No aplica");
        final String destination = tripStops.stream()
                .max(Comparator.comparing(TripStop::getStopOrder))
                .map(tripStop -> tripStop.getCity().getName())
                .orElse("No aplica");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        final String formattedDate = context.getStartTripDateTime().format(formatter);

        return NotificationPayloadDTO.builder()
                .pushTitle("Viaje cerrado por cupo completo")
                .pushBody(String.format("Tu viaje publicado, con origen %s hacia %s ha sido cerrado ya que alcanzó el cupo máximo.", origin, destination))
                .emailSubject(SUBJECT_TRIP_FULL)
                .emailTitle(TITLE_GREETING.replace("{name}", driverName))
                .emailMessage(MESSAGE_TRIP_FULL
                		.replace("{origin}", origin)
                		.replace("{destination}", destination)
                		.replace("{date}", formattedDate))
                .emailButtonText(null)
                .emailButtonUrl(null)
                .emailMessageFooter(MESSAGE_FOOTER_TRIP_FULL)
                .build();

    }
}
