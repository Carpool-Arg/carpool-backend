package com.carpool.carpool.controller.auth;

import com.carpool.carpool.dto.security.logout.LogoutRequestDTO;
import com.carpool.carpool.dto.security.token.TokenResponseDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.auth.IAuthService;
import com.carpool.carpool.service.auth.blacklist.IAuthBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthBlacklistService authBlacklistService;
    private final IAuthService authService;

    @Operation(
            summary = "Logout de la aplicación"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token correcto")
    })
    @PostMapping("/logout")
    public ResponseEntity<Response<Void>> logout(@RequestHeader("Authorization") String authHeader, @Valid @RequestBody LogoutRequestDTO logoutRequestDTO) {
        return new ResponseEntity<>(authBlacklistService.blacklistToken(authHeader,logoutRequestDTO), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Response<TokenResponseDTO>> refresh(@RequestHeader("Authorization") String refreshToken) {
        return new ResponseEntity<>(authService.refreshToken(refreshToken), HttpStatus.OK);
    }

    @GetMapping("/verify-token")
    public ResponseEntity<Response<Void>> verifyToken(@RequestHeader("Authorization") String authHeader) {
        return new ResponseEntity<>(authService.verifyToken(authHeader), HttpStatus.OK);
    }
}
