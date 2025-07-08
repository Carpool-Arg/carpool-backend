package com.carpool.carpool.service.auth;

import com.carpool.carpool.dto.security.token.TokenResponseDTO;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.security.utils.JwtUtils;

import static com.carpool.carpool.security.config.TokenJwtConfig.*;

import com.carpool.carpool.utils.ResponseUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthImplementation implements IAuthService{
    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserRepository userRepository;

    @Override
    public Response<TokenResponseDTO> refreshToken(String authHeader) {

        if(authHeader == null || !authHeader.startsWith(PREFIX_TOKEN)){
            throw new IllegalArgumentException("Bearer Token Inválido");
        }

        final String refreshToken = authHeader.substring(7);
        final String username = jwtUtils.extractUsername(refreshToken);

        if(username == null){
            throw new IllegalArgumentException("Refresh Token Inválido");
        }

        final User user = userRepository.findByUsername(username)
            .orElseThrow(()-> new UsernameNotFoundException("El nombre de usuario del token no existe en el sistema"));

        if (!jwtUtils.isTokenValid(refreshToken, user)){
            throw new IllegalArgumentException("Refresh Token Inválido");
        }

        // Clonar claims originales
        Claims claims = Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();

        final String accessToken = jwtUtils.generateToken(username,86400000,claims);

        TokenResponseDTO dto = new TokenResponseDTO(accessToken, refreshToken);

        return ResponseUtils.buildOKResponse(List.of("Token refrescado correctamente"), dto);
    }

}
