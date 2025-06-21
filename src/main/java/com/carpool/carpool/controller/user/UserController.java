package com.carpool.carpool.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carpool.carpool.dto.user.UserRequestDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.user.IUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


@RestController
@Tag(name = "Pasajeros", description = "Operaciones relacionadas con el pasajero")
@RequestMapping("/users")
public class UserController {
    @Autowired
    private IUserService userService;

    @Operation(
        summary = "Register a new user",
        description = "Registers a new user with validated input fields."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User successfully created"),
        @ApiResponse(responseCode = "400", description = "Validation error", 
            content = @Content(mediaType = "application/json"))
    })

    @PostMapping
    public ResponseEntity<Response<Void>> save(@Valid @RequestBody UserRequestDTO userRequestDTO) {
        return new ResponseEntity<>(userService.saveUser(userRequestDTO), HttpStatus.OK);
    }
}
