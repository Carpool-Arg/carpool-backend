package com.carpool.carpool.service.auth;

import com.carpool.carpool.dto.security.TokensDTO;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.security.utils.JwtUtils;

import static com.carpool.carpool.security.config.TokenJwtConfig.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class AuthServiceImplementation implements IAuthService{
    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserRepository userRepository;

    @Override
    public TokensDTO refreshToken(String authHeader) {

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

        

        final String accesToken = jwtUtils.generateToken(username, 0, null);
    }

}
