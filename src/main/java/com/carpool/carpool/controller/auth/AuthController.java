package com.carpool.carpool.controller.auth;

import com.carpool.carpool.dto.security.token.TokenResponseDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.auth.AuthImplementation;
import com.carpool.carpool.service.auth.blacklist.AuthBlacklistImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthBlacklistImplementation authBlacklistImplementation;

    @Autowired
    private AuthImplementation authImplementation;

    @PostMapping("/logout")
    public ResponseEntity<Response<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        return new ResponseEntity<>(authBlacklistImplementation.blacklistToken(authHeader), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Response<TokenResponseDTO>> refresh(@RequestHeader("Authorization") String refreshToken) {
        return new ResponseEntity<>(authImplementation.refreshToken(refreshToken), HttpStatus.OK);
    }
}
