package com.carpool.carpool.service.notification.content;

import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.model.driver.Driver;

import static com.carpool.carpool.utils.EmailMessageUtils.SUBJECT_LICENSE_APPROVED;
import static com.carpool.carpool.utils.EmailMessageUtils.TITLE_LICENSE_APPROVED;
import static com.carpool.carpool.utils.EmailMessageUtils.MESSAGE_LICENSE_APPROVED;
import static com.carpool.carpool.utils.EmailMessageUtils.FOOTER_LICENSE_APPROVED;

@Component
public class LicenseApprovedNotificationImplementation implements INotificationContentService<Driver> {

    @Override
    public NotificationEventEnum getEvent() {
        return NotificationEventEnum.LICENSE_APPROVED;
    }

    @Override
    public DispatchPolicyEnum getPolicy() {
        return DispatchPolicyEnum.EMAIL_ONLY;
    }

    @Override
    public NotificationPayloadDTO build(Driver context) {
        String driverName = context.getUser().getName();

        return NotificationPayloadDTO.builder()
                .pushTitle("Carnet aprobado")
                .pushBody("¡Tu carnet de conducir fue aprobado! Ya podés publicar viajes.")
                .emailSubject(SUBJECT_LICENSE_APPROVED)
                .emailTitle(TITLE_LICENSE_APPROVED.replace("{name}", driverName))
                .emailMessage(MESSAGE_LICENSE_APPROVED)
                .emailButtonText(null)
                .emailButtonUrl(null)
                .emailMessageFooter(FOOTER_LICENSE_APPROVED)
                .build();
    }
}