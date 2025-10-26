package com.carpool.carpool.service.notification.dispatcher;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.model.user.User;

public interface INotificationDispatcherService {
    void dispatch(User user, NotificationPayloadDTO payload, DispatchPolicyEnum policy);
}
