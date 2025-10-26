package com.carpool.carpool.service.notification.content;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;

public interface INotificationContentService<T>{
    /** El evento de negocio que esta clase maneja */
    NotificationEventEnum getEvent();

    /** La política de canal que esta notificación DEBE usar */
    DispatchPolicyEnum getPolicy();

    /** Construye el payload de notificación */
    NotificationPayloadDTO build(T context);
}
