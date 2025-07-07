package com.carpool.carpool.service.auth;

import com.carpool.carpool.dto.security.TokensDTO;

public interface IAuthService {
    TokensDTO refreshToken(String authHeader);
}
