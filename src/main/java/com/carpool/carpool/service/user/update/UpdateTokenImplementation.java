package com.carpool.carpool.service.user.update;

import com.carpool.carpool.dto.security.token.TokenResponseDTO;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.security.model.CustomUserDetails;
import com.carpool.carpool.security.utils.JwtUtils;
import com.carpool.carpool.service.auth.blacklist.IAuthBlacklistService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateTokenImplementation {
    private final IAuthBlacklistService authBlacklistService;
    private final HttpServletRequest request;

    @Transactional
    public TokenResponseDTO invalidateAllUserTokensAndGenerateNew(User user) {
        try {
            String[] currentTokens = getCurrentTokens();
            String currentAccessToken = currentTokens[0];
            String currentRefreshToken = currentTokens[1];

            if (currentAccessToken != null) {
                try {
                    authBlacklistService.blacklistAccessTokenOnly(currentAccessToken);
                } catch (Exception e) {
                    throw new ConflictException("Error al invalidar el access token: " + e.getMessage());
                }
            }

            if (currentRefreshToken != null) {
                try {
                    authBlacklistService.blacklistRefreshToken(currentRefreshToken);
                } catch (Exception e) {
                    throw new ConflictException("Error al invalidar el refresh token: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            throw new ConflictException("Error al invalidar los tokens: " + e.getMessage());
        }

        return generateTokensForUser(user);
    }

    public TokenResponseDTO generateTokensForUser(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);

        String authoritiesJson;
        try {
            authoritiesJson = new ObjectMapper().writeValueAsString(userDetails.getAuthorities());
        } catch (JsonProcessingException e) {
            throw new ConflictException("Error al serializar autoridades para JWT: " + e.getMessage());
        }

        Claims claims = Jwts.claims()
                .add("authorities", authoritiesJson)
                .add("username", userDetails.getUsername())
                .build();

        String accessToken = JwtUtils.generateAccessToken(userDetails.getUsername(), claims);
        String refreshToken = JwtUtils.generateRefreshToken(userDetails.getUsername(), claims);

        return new TokenResponseDTO(accessToken, refreshToken);
    }

    private String[] getCurrentTokens() {
        String accessToken = null;
        String refreshToken = null;

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        refreshToken = request.getHeader("X-Refresh-Token");

        if (accessToken == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getCredentials() instanceof String) {
                accessToken = (String) authentication.getCredentials();
            }
        }

        return new String[]{accessToken, refreshToken};
    }
}
