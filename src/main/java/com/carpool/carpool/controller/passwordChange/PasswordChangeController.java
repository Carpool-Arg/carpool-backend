package com.carpool.carpool.controller.passwordChange;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carpool.carpool.dto.user.ChangePasswordRequestDTO;
import com.carpool.carpool.dto.user.EmailRequestDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.user.IUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@Tag(name="Solicitud para recuperación de contraseña", description = "Opreacion para recuperar la contraseña en caso de olvidarla")
@RequestMapping("/password-change")
@RequiredArgsConstructor
public class PasswordChangeController {

    private final IUserService userService;

    @Operation(
        summary = "Realizar la solicitud para el cambio de contraseña",
        description = "Realizar la solicitud para el cambio de contraseña pasando el correo electronico para validar al usuario"
    )
    @ApiResponses(value={
        @ApiResponse(responseCode = "201", description = "Correo enviado con éxito.")
    })
    @PostMapping("/send-email")
    public ResponseEntity<Response<Void>> sendEmailPasswordRecovery(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request para solicitar el envio de correo electronico para la recuperacion de la contraseña.", required = true) 
        @Valid 
        @RequestBody EmailRequestDTO emailRequestDTO){
        
        Response<Void> serviceResponse = userService.sendPasswordChangeEmail(emailRequestDTO);
        return new ResponseEntity<>(serviceResponse,HttpStatus.OK);
    }


    @Operation(
        summary = "Realizar el cambio de contraseña",
        description = "Realizar el cambio de contraseña pasando la nueva contraseña, la confirmacion de la misma y el token correspondiente."
    )
    @ApiResponses(value={
        @ApiResponse(responseCode = "200", description = "Contraseña actualizada."),
        @ApiResponse(responseCode = "404", description = "Bad request")
    })
    @PostMapping
    public ResponseEntity<Response<Void>> changePassword(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request para realizar el cambio de contraseña.", required = true)
        @Valid 
        @RequestBody ChangePasswordRequestDTO changePasswordRequestDTO) {

        Response<Void> serviceResponse = userService.changePassword(changePasswordRequestDTO);
        return new ResponseEntity<>(serviceResponse,HttpStatus.OK);
    }
      
}
