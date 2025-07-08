package com.carpool.carpool.service.auth;

import com.carpool.carpool.dto.security.token.TokenResponseDTO;
import com.carpool.carpool.response.Response;

public interface IAuthService {
    Response<TokenResponseDTO> refreshToken(String authHeader);
}
