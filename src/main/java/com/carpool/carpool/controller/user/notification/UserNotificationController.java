package com.carpool.carpool.controller.user.notification;

import com.carpool.carpool.dto.user.UserTokenRequestDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.user.notification.IUserNotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name="Notificaciones push a usuarios", description = "Opreacion para enviar notificaciones a los usuarios")
@RequestMapping("/notification")
@RequiredArgsConstructor
public class UserNotificationController {

    private final IUserNotificationService userNotificationService;

    @PostMapping("/register")
    public ResponseEntity<Response<Void>> registerToken(@RequestBody UserTokenRequestDTO userTokenRequestDTO) {

        return new ResponseEntity<>(userNotificationService.register(userTokenRequestDTO), HttpStatus.OK);
    }
}
