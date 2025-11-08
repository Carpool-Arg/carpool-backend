package com.carpool.carpool.dto.notificationPayload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationPayloadDTO {
    // --- Campos para PUSH NOTIFICATION ---
    private String pushTitle;

    private String pushBody;

    // --- Campos para EMAIL ---
    private String emailSubject;

    private String emailTitle;

    private String emailMessage;

    private String emailOptionalMessage;

    private String emailButtonUrl;

    private String emailButtonText;

    private String emailMessageFooter;
}
