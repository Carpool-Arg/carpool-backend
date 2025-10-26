package com.carpool.carpool.service.notification.dispatch;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.model.user.User;

public interface INotificationDispatchPolicyService {
    void execute(User user, NotificationPayloadDTO payload);
    DispatchPolicyEnum getPolicy();
}
