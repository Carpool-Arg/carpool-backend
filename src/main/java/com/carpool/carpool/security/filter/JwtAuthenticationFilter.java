package com.carpool.carpool.security.filter;

import static com.carpool.carpool.security.config.TokenJwtConfig.CONTENT_TYPE;
import static com.carpool.carpool.security.config.TokenJwtConfig.HEADER_AUTHORIZATION;
import static com.carpool.carpool.security.config.TokenJwtConfig.PREFIX_TOKEN;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.carpool.carpool.dto.security.token.TokenResponseDTO;
import com.carpool.carpool.dto.security.login.loginRequestDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.security.model.CustomUserDetails;
import com.carpool.carpool.security.utils.JwtUtils;
import com.carpool.carpool.utils.ResponseUtils;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro personalizado que intercepta peticiones realizadas al endpoint {@link /login} y se encarga de autenticar al usuario con sus credenciales (username y password).
 *
 * Si la autenticación es exitosa, se genera un token JWT y lo devuelve en la response.
 * Si la autenticación no es exitosa, se retorna un mensaje de error personalizado.
 */
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter{

    public final static String AUTHORITIES = "authorities";
    private final static String USERNAME = "username";

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private AuthenticationManager authenticationManager;

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Método que se ejecuta cuando el cliente realiza una petición de login.
     *
     * Obtiene el username y password de la petición y genera un token de autenticación con los mismos, que luego es validado por el
     * {@link AuthenticationManager}.
     *
     * @param request petición HTTP.
     * @param response respuesta HTTP.
     * @return Respuesta de la autenticación.
     * @throws AuthenticationException si ocurre algún error durante la autenticación.
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        loginRequestDTO userLogin = null;

        try {
            userLogin = new ObjectMapper().readValue(request.getInputStream(), loginRequestDTO.class);
        } catch (StreamReadException e) {
        } catch (IOException e) {
            throw new RuntimeException("Error al leer las credenciales de la petición", e);
        }
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userLogin.getUsername(), userLogin.getPassword());

        return authenticationManager.authenticate(authToken);
    }

    /**
     * Método que se invoca cuando la autenticación es exitosa. Obtiene el usuario autenticado y genera el token JWT.
     *
     * @param request petición HTTP.
     * @param response respuesta HTTP.
     * @param chain cadena de filtros de seguridad.
     * @param authResult resultado de la autenticación.
     *
     * @throws IOException si ocurre algún error durante la autenticación.
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {

        // Obtener el usuario autenticado casteado a CustomUserDetails
        CustomUserDetails authenticatedUser = (CustomUserDetails) authResult.getPrincipal();

        // Extraer el nombre de usuario del usuario autenticado
        String username = authenticatedUser.getUsername();

        // Obtener los roles/authorities del usuario para incluirlos en el token
        Collection<? extends GrantedAuthority> authorities = authenticatedUser.getAuthorities();

        LOGGER.info("AUTENTICACION EXITOSA DEL USUARIO: {}", username);

        // Construir los claims para el JWT, agregando roles y username
        Claims claims = Jwts.claims()
                .add(AUTHORITIES, new ObjectMapper().writeValueAsString(authorities))
                .add(USERNAME, username)
        .build();

        // Generar el access token con duración de 1 día (86400000 ms) y los claims
        String accessToken = jwtUtils.generateToken(username, 86400000, claims);

        // Generar el refresh token con duración de 7 días (604800000 ms) y los mismos claims
        String refreshToken = jwtUtils.generateToken(username, 604800000, claims);

        // Agregar el access token en el header Authorization de la respuesta HTTP
        response.addHeader(HEADER_AUTHORIZATION, PREFIX_TOKEN + accessToken);

        // Crear un DTO que contiene ambos tokens para enviarlo en el cuerpo de la respuesta
        TokenResponseDTO tokens = new TokenResponseDTO(accessToken,refreshToken);

        // Construir el ResponseEntity con mensaje de éxito, DTO y código HTTP 200 OK
        ResponseEntity<Response<TokenResponseDTO>> responseBody = new ResponseEntity<>(
            ResponseUtils.buildOKResponse(
                List.of(username + ": Ha iniciado sesión exitosamente."),
                tokens
            ), 
            HttpStatus.OK
        );

        // Escribir el cuerpo de la respuesta en formato JSON y enviarla al cliente
        ResponseUtils.writeResponse(response, responseBody, CONTENT_TYPE);
    }

    /**
     * Método que se invoca cuando la autenticación no es exitosa.
     *
     * @param request petición HTTP.
     * @param response respuesta HTTP.
     *
     * @throws IOException si ocurre algún error durante el proceso.
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {

        ResponseEntity<Response<Void>> responseBody = new ResponseEntity<>(
            ResponseUtils.buildErrorResponse(List.of( "Error en la autenticación")),
            HttpStatus.UNAUTHORIZED
        );

        ResponseUtils.writeResponse(response, responseBody, CONTENT_TYPE);
    }
}
