package com.carpool.carpool.controller.user;

import com.carpool.carpool.dto.user.UserUpdateRequestDTO;
import lombok.RequiredArgsConstructor;
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

@RestController
@Tag(name = "Pasajeros", description = "Operaciones relacionadas con el pasajero")
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

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
            @Valid
            @RequestParam String email,
            @RequestBody UserUpdateRequestDTO userUpdateRequestDTO) {
        return new ResponseEntity<>(userService.updateUser(userUpdateRequestDTO, email), HttpStatus.OK);
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