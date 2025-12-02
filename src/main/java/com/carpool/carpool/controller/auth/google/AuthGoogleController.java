package com.carpool.carpool.controller.auth.google;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carpool.carpool.dto.google.GoogleAuthRequestDTO;
import com.carpool.carpool.dto.google.GoogleAuthResponse;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.auth.google.IAuthGoogleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Auth Google", description = "Operaciones relacionadas con autenticacion con Google")
@RequestMapping("/auth-google")
@RequiredArgsConstructor
public class AuthGoogleController {

    private final IAuthGoogleService googleAuthService;

    @Operation(
            summary = "Validar token ID Google"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token correcto"),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Response<GoogleAuthResponse>> authenticateWithGoogle(@Valid @RequestBody GoogleAuthRequestDTO request) {
        return new ResponseEntity<>(googleAuthService.authenticate(request.getIdToken()), HttpStatus.OK);
    }
}
