package com.carpool.carpool.service.user.notification;

import com.carpool.carpool.dto.user.UserTokenRequestDTO;
import com.carpool.carpool.response.Response;

public interface IUserNotificationService {
    /**
     * Registra el token de notificaciones push para el usuario autenticado.
     *
     * @param userTokenRequestDTO DTO con el token de notificación push enviado desde el frontend.
     * @return Response<Void> indicando el resultado de la operación.
     */
    Response<Void> register(UserTokenRequestDTO userTokenRequestDTO);
}
