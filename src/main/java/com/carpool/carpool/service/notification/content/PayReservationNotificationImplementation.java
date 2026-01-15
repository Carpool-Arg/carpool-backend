package com.carpool.carpool.service.notification.content;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.model.reservation.Reservation;

import static com.carpool.carpool.utils.EmailMessageUtils.*;
import static com.carpool.carpool.utils.EmailMessageUtils.BUTTON_NEW_RESERVATION;
import static com.carpool.carpool.utils.EmailMessageUtils.MESSAGE_FOOTER_NEW_RESERVATION;

public class PayReservationNotificationImplementation implements INotificationContentService<Reservation>{
    @Override
    public NotificationEventEnum getEvent() { return NotificationEventEnum.RESERVATION_UNPAID; }

    @Override
    public DispatchPolicyEnum getPolicy() { return DispatchPolicyEnum.WEB_SOCKET; }

    @Override
    public NotificationPayloadDTO build(Reservation reservation) {
        String passengerName = reservation.getUser().getName();
        return NotificationPayloadDTO.builder()
                .pushTitle("¡Pagá tu Viaje!")
                .pushBody(passengerName + " paga el viaje")
                .build();
    }
}
