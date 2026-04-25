package com.carpool.carpool.service.notification.content;



import static com.carpool.carpool.utils.EmailMessageUtils.*;

import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.model.reservation.Reservation;


@Component
public class PassengerDeletedFromTripNotificationImplementation implements INotificationContentService<Reservation>{
  @Override
  public NotificationEventEnum getEvent() {return NotificationEventEnum.PASSENGER_DELETED_FROM_TRIP;}

  @Override
  public DispatchPolicyEnum getPolicy() {return DispatchPolicyEnum.EMAIL_ONLY;}

    @Override
    public NotificationPayloadDTO build(Reservation reservation) {
        String baseMessage = MESSAGE_RESERVATION_CANCELLED_BY_DRIVER
            .replace("{driver}", reservation.getTrip().getVehicle().getDriver().getUser().getName())
            .replace("{origin}", reservation.getStartCity().getCity().getName())
        .replace("{destination}", reservation.getDestinationCity().getCity().getName());

        String reason = reservation.getCancellationReason();
        
        if (reason != null && !reason.isBlank()) {
            baseMessage += MESSAGE_RESERVATION_CANCELLED_BY_DRIVER_REASON
            .replace("{reason}", reason);
        }
        return NotificationPayloadDTO.builder()
            .emailSubject(SUBJECT_RESERVATION_CANCELLED_BY_DRIVER)
            .emailTitle(TITLE_RESERVATION_CANCELLED_BY_DRIVER)
            .emailMessage(baseMessage)
            .emailMessageFooter(FOOTER_RESERVATION_CANCELLED_BY_DRIVER)
        .build();
    }
}
