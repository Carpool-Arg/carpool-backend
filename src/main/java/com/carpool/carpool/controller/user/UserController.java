package com.carpool.carpool.controller.user;

import com.carpool.carpool.dto.security.token.TokenResponseDTO;
import com.carpool.carpool.dto.user.TokenRequestDTO;
import com.carpool.carpool.dto.user.UserEmailChangeRequestDTO;
import com.carpool.carpool.dto.user.UserPasswordChangeRequestDTO;
import com.carpool.carpool.dto.user.UserProfileUpdateRequestDTO;
import com.carpool.carpool.dto.user.EmailRequestDTO;
import com.carpool.carpool.dto.user.UserUpdateRequestDTO;
import com.carpool.carpool.enums.user.UserGenderEnum;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.carpool.carpool.dto.user.UserRequestDTO;
import com.carpool.carpool.dto.user.UserResponseDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.user.IUserService;
import com.carpool.carpool.utils.ResponseUtils;

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
        return new ResponseEntity<>(userService.activateAccount(tokenRequestDTO.getToken()), HttpStatus.OK);
    }

    @Operation(summary = "Reenvio de correo para activar la cuenta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correo electrónico enviado", content = @Content)
    })
    @PostMapping("/resend-activation")
    public ResponseEntity<Response<Void>> resendActivateAccount(@RequestBody EmailRequestDTO userActivationRequestDTO) {
        return new ResponseEntity<>(userService.resendActivateAccount(userActivationRequestDTO.getEmail()), HttpStatus.OK);
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

    @Operation(summary = "Obtener lista de géneros disponibles")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de géneros recuperada con éxito")
    })
    @GetMapping("/genders")
    public ResponseEntity<Response<List<String>>> getAvailableGenders() {
        List<String> genders = Arrays.stream(UserGenderEnum.values())
                                    .map(Enum::name)
                                    .collect(Collectors.toList());
        return new ResponseEntity<>(ResponseUtils.buildOKResponse(List.of("Lista de géneros"), genders), HttpStatus.OK);
    }

    @Operation(summary = "Obtener el usuario autenticado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario autenticado recuperado con éxito"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping()
    public ResponseEntity<Response<UserResponseDTO>> getAuthenticatedUser() {
        return new ResponseEntity<>(userService.getAuthenticatedUser(), HttpStatus.OK);
    }

    @Operation(summary = "Actualizar datos del perfil del usuario")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Perfil actualizado correctamente"),
        @ApiResponse(responseCode = "409", description = "Errores de validación"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
    })
    @PutMapping(value = "/update-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<TokenResponseDTO>> updateProfile(
                @RequestPart("userProfileUpdateRequestDTO") UserProfileUpdateRequestDTO userProfileUpdateRequestDTO,
                @RequestPart(value = "file", required = false) MultipartFile file) {

        return new ResponseEntity<>(userService.updateUserProfile(userProfileUpdateRequestDTO, file), HttpStatus.OK);
    }

    @Operation(summary = "Actualizar la contraseña del usuario autenticado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contraseña actualizada correctamente"),
        @ApiResponse(responseCode = "409", description = "Conflicto de validaciones"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
    })
    @PutMapping("/update-password")
    public ResponseEntity<Response<TokenResponseDTO>> updatePassword(@RequestBody @Valid UserPasswordChangeRequestDTO userPasswordChangeRequestDTO) {
        return new ResponseEntity<>(userService.updateUserPassword(userPasswordChangeRequestDTO), HttpStatus.OK);
    }

    @Operation(summary = "Actualizar el correo electrónico del usuario autenticado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cambio de correo iniciado. Confirmación requerida."),
        @ApiResponse(responseCode = "409", description = "Conflicto de validaciones"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
    })
    @PutMapping("/update-email")
    public ResponseEntity<Response<?>> updateEmail(@RequestBody @Valid UserEmailChangeRequestDTO userEmailChangeRequestDTO) {
        return new ResponseEntity<>(userService.updateUserEmail(userEmailChangeRequestDTO), HttpStatus.OK);
    }

    @Operation(summary = "Confirmar el cambio de correo electrónico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Correo electrónico actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Token no encontrado"),
        @ApiResponse(responseCode = "409", description = "Token inválido o expirado", content = @Content)
    })
    @PostMapping("/confirm-email-change")
    public ResponseEntity<Response<Void>> confirmEmailChange(@RequestBody TokenRequestDTO tokenRequestDTO) {
        return new ResponseEntity<>(userService.confirmEmailChange(tokenRequestDTO.getToken()), HttpStatus.OK);
    }

}