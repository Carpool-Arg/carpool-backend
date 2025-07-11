package com.carpool.carpool.controller.auth;

import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.auth.blacklist.AuthBlacklistImplementation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private AuthBlacklistImplementation authBlacklistImplementation;

    @Operation(
            summary = "Logout de la aplicación"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token correcto")
    })
    @PostMapping("/logout")
    public ResponseEntity<Response<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        return new ResponseEntity<>(authBlacklistImplementation.blacklistToken(authHeader), HttpStatus.OK);
    }
}
