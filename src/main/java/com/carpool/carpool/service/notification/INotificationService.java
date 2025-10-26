package com.carpool.carpool.service.notification;

import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.model.user.User;

public interface INotificationService {
    <T> void send(User userToNotify, NotificationEventEnum event, T context);
}
