package com.carpool.carpool.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.carpool.carpool.service.user.IUserService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Pasajeros", description = "Operaciones relacionadas con el pasajero")
public class UserController {
    @Autowired
    private IUserService userService;

}
