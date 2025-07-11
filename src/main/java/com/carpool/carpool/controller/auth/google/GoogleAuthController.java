package com.carpool.carpool.controller.auth.google;

import com.carpool.carpool.dto.user.google.GoogleAuthRequestDTO;
import com.carpool.carpool.dto.user.google.GoogleAuthResponse;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.auth.google.IGoogleAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Auth Google", description = "Operaciones relacionadas con autenticacion con Google")
@RequestMapping("/auth")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final IGoogleAuthService googleAuthService;

    @Operation(
            summary = "Validar token ID Google"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token correcto"),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content)
    })
    @PostMapping("/google")
    public ResponseEntity<Response<GoogleAuthResponse>> authenticateWithGoogle(@Valid @RequestBody GoogleAuthRequestDTO request){
        return new ResponseEntity<>(googleAuthService.authenticate(request.getIdToken()), HttpStatus.OK);
    }
}
