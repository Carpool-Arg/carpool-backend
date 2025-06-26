package com.carpool.carpool.controller.user;

import com.carpool.carpool.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.carpool.carpool.dto.user.UserRequestDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.user.IUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;


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
        //TODO: Cambiar ya que siempre se retorna 200 aunque la peticion sea con estado ERROR
        return new ResponseEntity<>(userService.saveUser(userRequestDTO), HttpStatus.CREATED);
    }
    

    @GetMapping("/validate-username")
    public ResponseEntity<Response> validateUsername(@RequestParam String username) {
        userService.validateUsername(username);
        return ResponseEntity.ok(ResponseUtils.buildOKResponse(List.of("Nombre de usuario disponible"), null));
    }

    @GetMapping("/validate-email")
    public ResponseEntity<Response> validateEmail(@RequestParam String email){
    userService.validateEmail(email);
        return ResponseEntity.ok(ResponseUtils.buildOKResponse(List.of("El Email del usuario disponible"), null));
    } 

    @GetMapping("/validate-dni")
    public ResponseEntity<Response> validateDni(@RequestParam String dni) {
        userService.validateDni(dni);
        return ResponseEntity.ok(ResponseUtils.buildOKResponse(List.of("El DNI del usuario disponible"), null));
    }
}