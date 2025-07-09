package com.carpool.carpool.controller.auth;

import com.carpool.carpool.dto.security.logout.LogoutRequestDTO;
import com.carpool.carpool.dto.security.token.TokenResponseDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.auth.IAuthService;
import com.carpool.carpool.service.auth.blacklist.IAuthBlacklistService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private IAuthBlacklistService authBlacklistService;

    @Autowired
    private IAuthService authService;

    @PostMapping("/logout")
    public ResponseEntity<Response<Void>> logout(@RequestHeader("Authorization") String authHeader, @Valid @RequestBody LogoutRequestDTO logoutRequestDTO) {
        return new ResponseEntity<>(authBlacklistService.blacklistToken(authHeader,logoutRequestDTO), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Response<TokenResponseDTO>> refresh(@RequestHeader("Authorization") String refreshToken) {
        return new ResponseEntity<>(authService.refreshToken(refreshToken), HttpStatus.OK);
    }
}
