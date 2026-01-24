package com.carpool.carpool.dto.notificationPayload;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class NotificationPayloadDTO {
    // --- Campos para PUSH/WS NOTIFICATION ---
    private String type;

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

    //Map para agregar información extra
    private Map<String, Object> data;
}
