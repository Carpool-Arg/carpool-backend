package com.carpool.carpool.controller.auth;

import com.carpool.carpool.dto.security.logout.LogoutRequestDTO;
import com.carpool.carpool.dto.security.token.TokenResponseDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.auth.IAuthService;
import com.carpool.carpool.service.auth.blacklist.IAuthBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Operaciones relacionadas con autenticacion")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthBlacklistService authBlacklistService;
    private final IAuthService authService;

    @Operation(
            summary = "Realizar logout en la aplicación"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesión cerrada correctamente"),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autorizado. Token de acceso inválido o ausente", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno. No se pudo acceder a Redis", content = @Content)
    })
    @PostMapping("/logout")
    public ResponseEntity<Response<Void>> logout(
            @Parameter(
                    description = "Encabezado Authorization con el access token. Formato: Bearer <token>",
                    required = true,
                    example = "Bearer eyJhbG..."
            )
            @RequestHeader("Authorization") String authHeader,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token para cerrar sesión",
                    required = true
            )
            @Valid @RequestBody LogoutRequestDTO logoutRequestDTO) {
        return new ResponseEntity<>(authBlacklistService.blacklistToken(authHeader,logoutRequestDTO), HttpStatus.OK);
    }

    @Operation(
            summary = "Actualizar access token",
            description = "Genera un nuevo access token a partir de un refresh token válido. "
                    + "El refresh token debe enviarse en el encabezado Authorization con el prefijo 'Bearer '."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refrescado correctamente"),
            @ApiResponse(responseCode = "400", description = "El refresh token es inválido o está mal formado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autorizado. El refresh token expiró o es inválido", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno al procesar el token", content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<Response<TokenResponseDTO>> refresh(
            @Parameter(
                    description = "Encabezado Authorization con el refresh token. Formato: Bearer <token>",
                    required = true,
                    example = "Bearer eyJhbG..."
            )
            @RequestHeader("Authorization") String refreshToken)
    {
        return new ResponseEntity<>(authService.refreshToken(refreshToken), HttpStatus.OK);
    }

    @Operation(
            summary = "Verificar validez del access token",
            description = "Verifica si el token JWT de acceso es válido (firma, expiración, estructura). "
                    + "El token debe enviarse en el encabezado Authorization con el formato: Bearer <token>."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token válido"),
            @ApiResponse(responseCode = "400", description = "El token es inválido o está mal formado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autorizado. Token expirado o inválido", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno al validar el token", content = @Content)
    })
    @GetMapping("/verify-token")
    public ResponseEntity<Response<Void>> verifyToken(
            @Parameter(
                    description = "Encabezado Authorization con el refresh token. Formato: Bearer <token>",
                    required = true,
                    example = "Bearer eyJhbG..."
            )
            @RequestHeader("Authorization") String authHeader
    ) {
        return new ResponseEntity<>(authService.verifyToken(authHeader), HttpStatus.OK);
    }
}
