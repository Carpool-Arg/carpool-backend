package com.carpool.carpool.service.auth;

import com.carpool.carpool.dto.security.token.TokenResponseDTO;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.security.utils.JwtUtils;
import com.carpool.carpool.service.auth.blacklist.IAuthBlacklistService;
import com.carpool.carpool.utils.ResponseUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import static com.carpool.carpool.security.config.TokenJwtConfig.*;

import java.util.List;
/**
 * Servicio de autenticación que implementa la lógica para el refresh de tokens JWT.
 * Se encarga de validar el refresh token, verificar la blacklist y emitir un nuevo access token.
 */
@Service
public class AuthImplementation implements IAuthService{
    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private IAuthBlacklistService authBlacklistService;

    /**
     * Genera un nuevo access token a partir de un refresh token válido.
     *
     * @param authHeader Encabezado Authorization con el refresh token (formato: "Bearer <token>")
     * @return Response<TokenResponseDTO> con el nuevo access token y el mismo refresh token
     * @throws IllegalArgumentException si el token es inválido, está en la blacklist o ha expirado
     */
    @Override
    public Response<TokenResponseDTO> refreshToken(String authHeader) {

        // Validar formato del encabezado Authorization
        if(authHeader == null || !authHeader.startsWith(PREFIX_TOKEN)){
            throw new IllegalArgumentException("Bearer Token Inválido");
        }

        // Extraer el token desde el header (sin "Bearer ")
        final String refreshToken = authHeader.substring(7);

        // Verificar si está en la blacklist
        if (authBlacklistService.isTokenBlacklisted(refreshToken)) {
            throw new IllegalArgumentException("Refresh Token inválido o ya expirado");
        }

        // Extraer el username desde el token
        final String username = jwtUtils.extractUsername(refreshToken);

        if(username == null){
            throw new IllegalArgumentException("Refresh Token Inválido");
        }

        // Buscar al usuario en base de datos
        final User user = userRepository.findByUsername(username)
            .orElseThrow(()-> new UsernameNotFoundException("El nombre de usuario del token no existe en el sistema"));

        // Validar el token contra los datos del usuario (firma, expiración, etc.)
        if (!jwtUtils.isTokenValid(refreshToken, user)){
            throw new IllegalArgumentException("Refresh Token Inválido");
        }

        // Clonar los claims del token original para reutilizarlos en el nuevo access token
        Claims claims = Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();

        // Generar nuevo access token (válido por 1 día)
        final String accessToken = jwtUtils.generateToken(username,86400000,claims);

        // Construir el DTO de respuesta con el nuevo access token y el mismo refresh token
        TokenResponseDTO dto = new TokenResponseDTO(accessToken, refreshToken);

        // Devolver respuesta exitosa con mensaje y DTO
        return ResponseUtils.buildOKResponse(List.of("Token refrescado correctamente"), dto);
    }
}
