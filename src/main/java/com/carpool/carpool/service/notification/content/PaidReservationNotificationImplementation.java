package com.carpool.carpool.service.notification.content;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.model.reservation.Reservation;
import org.springframework.stereotype.Component;

import static com.carpool.carpool.utils.EmailMessageUtils.*;

@Component
public class PaidReservationNotificationImplementation implements INotificationContentService<Reservation>{

    @Override
    public NotificationEventEnum getEvent() {return NotificationEventEnum.RESERVATION_PAID;}

    @Override
    public DispatchPolicyEnum getPolicy() {return DispatchPolicyEnum.EMAIL_ONLY;}

    @Override
    public NotificationPayloadDTO build(Reservation reservation) {
        String passengerName = reservation.getUser().getName();
        String driverName = reservation.getTrip().getVehicle().getDriver().getUser().getName();
        Double total = reservation.getTotal() - (reservation.getTrip().getDriverPriceDiscount() * 2);

        return NotificationPayloadDTO.builder()
                .emailSubject(SUBJECT_EMAIL_RESERVATION_PAID)
                .emailTitle(TITLE_RESERVATION_PAID.replace("{name}", driverName ))
                .emailMessage(
                        MESSAGE_RESERVATION_PAID
                                .replace("{name}", passengerName)
                                .replace("{total}", String.valueOf(total))
                )
                .emailMessageFooter(MESSAGE_FOOTER_RESERVATION_PAID)
                .build();
    }
}
