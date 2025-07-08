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
import com.carpool.carpool.dto.user.UserLoginDTO;
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

        UserLoginDTO userLogin = null;

        try {
            userLogin = new ObjectMapper().readValue(request.getInputStream(), UserLoginDTO.class);
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

        CustomUserDetails authenticatedUser = (CustomUserDetails) authResult.getPrincipal();
        String username = authenticatedUser.getUsername();
        Collection<? extends GrantedAuthority> authorities = authenticatedUser.getAuthorities();

        LOGGER.info("AUTENTICACION EXITOSA DEL USUARIO: {}", username);

        Claims claims = Jwts.claims()
                .add(AUTHORITIES, new ObjectMapper().writeValueAsString(authorities))
                .add(USERNAME, username)
        .build();

        String accessToken = jwtUtils.generateToken(username, 86400000, claims);
        
        // Jwts.builder()
        //         .subject(username)
        //         .claims(claims)
        //         .expiration(new Date(System.currentTimeMillis() + 86400000)) // Un dia de duracion
        //         .issuedAt(new Date())
        //         .signWith(SECRET_KEY)
        //         .compact();


        String refreshToken = jwtUtils.generateToken(username, 604800000, claims);
        
        // String refreshToken = Jwts.builder()
        //         .subject(username)
        //         .claims(claims)
        //         .expiration(new Date(System.currentTimeMillis() + 604800000)) // 7 dias de duracion 
        //         .issuedAt(new Date())
        //         .signWith(SECRET_KEY)
        //         .compact();

        response.addHeader(HEADER_AUTHORIZATION, PREFIX_TOKEN + accessToken);

        TokenResponseDTO tokens = new TokenResponseDTO(accessToken,refreshToken);
        
        ResponseEntity<Response<TokenResponseDTO>> responseBody = new ResponseEntity<>(
            ResponseUtils.buildOKResponse(
                List.of(username + ": Ha iniciado sesión exitosamente."),
                tokens
            ), 
            HttpStatus.OK
        );

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
