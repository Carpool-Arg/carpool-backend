package com.carpool.carpool.service.notification.content;

import static com.carpool.carpool.utils.EmailMessageUtils.FOOTER_LICENSE_REJECTED;
import static com.carpool.carpool.utils.EmailMessageUtils.SUBJECT_LICENSE_REJECTED;
import static com.carpool.carpool.utils.EmailMessageUtils.TITLE_LICENSE_REJECTED;
import static com.carpool.carpool.utils.EmailMessageUtils.MESSAGE_LICENSE_REJECTED;

import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.model.driver.Driver;

@Component
public class LicenseRejectedNotificationImplementation implements INotificationContentService<Driver> {

    @Override
    public NotificationEventEnum getEvent() {
        return NotificationEventEnum.LICENSE_REJECTED;
    }

    @Override
    public DispatchPolicyEnum getPolicy() {
        return DispatchPolicyEnum.EMAIL_ONLY;
    }

    @Override
    public NotificationPayloadDTO build(Driver context) {
        String driverName = context.getUser().getName();
        String reason = context.getRejectionReason() != null 
                ? context.getRejectionReason() 
                : "No se especificó un motivo.";

        return NotificationPayloadDTO.builder()
                .pushTitle("Carnet rechazado")
                .pushBody(String.format("Tu carnet de conducir fue rechazado. Motivo: %s", reason))
                .emailSubject(SUBJECT_LICENSE_REJECTED)
                .emailTitle(TITLE_LICENSE_REJECTED.replace("{name}", driverName))
                .emailMessage(MESSAGE_LICENSE_REJECTED.replace("{reason}", reason))
                .emailButtonText(null)
                .emailButtonUrl(null)
                .emailMessageFooter(FOOTER_LICENSE_REJECTED)
                .build();
    }
}