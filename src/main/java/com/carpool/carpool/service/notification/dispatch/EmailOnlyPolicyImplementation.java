package com.carpool.carpool.service.notification.dispatch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.service.email.IEmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailOnlyPolicyImplementation implements INotificationDispatchPolicyService {
    
    private final IEmailService emailService; 

    @Override
    public boolean execute(User user, NotificationPayloadDTO payload) {
        try {
            if (payload.getEmailAttachmentBytes() != null) {
                emailService.sendEmailWithAttachment(
                        user.getEmail(),
                        payload.getEmailSubject(),
                        payload.getEmailTitle(),
                        payload.getEmailMessage(),
                        payload.getEmailOptionalMessage(),
                        payload.getEmailButtonUrl(),
                        payload.getEmailButtonText(),
                        payload.getEmailMessageFooter(),
                        payload.getEmailAttachmentBytes(),
                        payload.getEmailAttachmentFilename()
                );
            } else {
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

            return true; // Envío exitoso
        } catch (Exception e) {
            log.error("Error enviando email a {}: {}", user.getEmail(), e.getMessage());
            return false; // El despacho falló
        }
    }

    @Override
    public DispatchPolicyEnum getPolicy() {
        return DispatchPolicyEnum.EMAIL_ONLY;
    }
    
}
