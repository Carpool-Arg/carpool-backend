package com.carpool.carpool.service.notification.dispatch;

import org.springframework.stereotype.Service;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.service.email.IEmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailOnlyPolicyImplementation implements INotificationDispatchPolicyService {
    
    private final IEmailService emailService; 

    @Override
    public void execute(User user, NotificationPayloadDTO payload) {
        emailService.sendEmail(
            user.getEmail(),
            payload.getEmailSubject(),
            payload.getEmailTitle(),
            payload.getEmailMessage(),
            payload.getEmailOptionalMessage(),
            payload.getEmailButtonUrl(),
            payload.getEmailButtonText(),
            payload.getEmailMessageFooter()
        );
    }

    @Override
    public DispatchPolicyEnum getPolicy() {
        return DispatchPolicyEnum.EMAIL_ONLY;
    }
    
}
