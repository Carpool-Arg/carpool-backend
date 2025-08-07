package com.carpool.carpool.controller.user;

import com.carpool.carpool.dto.user.*;
import com.carpool.carpool.service.user.account.IUserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final IUserAccountService userAccountService;

    @Operation(
            summary = "Registrar un nuevo usuario"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado con exito"),
            @ApiResponse(responseCode = "404", description = "Bad request"),
            @ApiResponse(responseCode = "409", description = "Errores de validaciones", content = @Content)
    })
    @PostMapping()
    public ResponseEntity<Response<Void>> save(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request para crear un usuario", required = true)
            @Valid
            @RequestBody UserRequestDTO userRequestDTO) {
        return new ResponseEntity<>(userService.saveUser(userRequestDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Completado del registro parcial de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado con exito"),
            @ApiResponse(responseCode = "404", description = "Bad request"),
            @ApiResponse(responseCode = "409", description = "Errores de validaciones", content = @Content)
    })
    @PostMapping("/complete-registration")
    public ResponseEntity<Response<Void>> update(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request para completar el registro parcial de un usuario", required = true)
            @Valid @RequestBody UserUpdateRequestDTO userUpdateRequestDTO) {
        return new ResponseEntity<>(userService.updateUser(userUpdateRequestDTO), HttpStatus.OK);
    }

    @Operation(summary = "Activar la cuenta de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario activado"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado"),
            @ApiResponse(responseCode = "409", description = "Erorres relacionados al token", content = @Content)
    })
    @PostMapping("/activate-account")
    public ResponseEntity<Response<Void>> activateAccount(@RequestBody TokenRequestDTO tokenRequestDTO) {
        return new ResponseEntity<>(userAccountService.activateAccount(tokenRequestDTO.getToken()), HttpStatus.OK);
    }

    @Operation(summary = "Reenvio de correo para activar la cuenta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correo electrónico enviado", content = @Content)
    })
    @PostMapping("/resend-activation")
    public ResponseEntity<Response<Void>> resendActivateAccount(@RequestBody EmailRequestDTO emailRequestDTO) {
        return new ResponseEntity<>(userService.resendActivateAccount(emailRequestDTO.getEmail()), HttpStatus.OK);
    }

    @Operation(summary = "Reenvio de correo para desbloquear la cuenta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correo electrónico enviado", content = @Content)
    })
    @PostMapping("/unlock-account")
    public ResponseEntity<Response<Void>> unlockAccount(@RequestBody ChangePasswordRequestDTO changePasswordRequestDTO) {
        return new ResponseEntity<>(userAccountService.unlockAccount(changePasswordRequestDTO), HttpStatus.OK);
    }

    @Operation(summary = "Validar si un username se encuentra en uso")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Username disponible"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "409", description = "Username no disponible"),
            @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
    })
    @GetMapping("/validate-username")
    public ResponseEntity<Response<Void>> validateUsername(@RequestParam String username) {
        return new ResponseEntity<>(userService.validateUsername(username), HttpStatus.OK);
    }

    @Operation(summary = "Validar si un email se encuentra en uso")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email disponible"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "409", description = "Email no disponible"),
            @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
    })
    @GetMapping("/validate-email")
    public ResponseEntity<Response<Void>> validateEmail(@RequestParam String email){
        return new ResponseEntity<>(userService.validateEmail(email), HttpStatus.OK);
    }

    @Operation(summary = "Validar si un DNI se encuentra en uso")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "DNI disponible"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "409", description = "DNI no disponible"),
            @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
    })
    @GetMapping("/validate-dni")
    public ResponseEntity<Response<Void>> validateDni(@RequestParam String dni) {
        return new ResponseEntity<>(userService.validateDni(dni), HttpStatus.OK);
    }
}