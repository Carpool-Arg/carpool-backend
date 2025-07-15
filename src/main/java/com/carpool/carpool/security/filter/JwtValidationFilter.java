package com.carpool.carpool.security.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.carpool.carpool.service.auth.blacklist.IAuthBlacklistService;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.security.utils.SimpleGrantedAuthorityJsonCreator;
import com.carpool.carpool.utils.ResponseUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.carpool.carpool.security.config.TokenJwtConfig.*;

/**
 * Clase que se encarga de validar si el JWT es válido.
 */
public class JwtValidationFilter extends BasicAuthenticationFilter{

    private static final ObjectMapper mapper = new ObjectMapper()
            .addMixIn(SimpleGrantedAuthority.class, SimpleGrantedAuthorityJsonCreator.class);

    private final IAuthBlacklistService authBlacklistService;
    private final UserRepository userRepository;

    public JwtValidationFilter(AuthenticationManager authenticationManager, IAuthBlacklistService authBlacklistService, UserRepository userRepository) {
        super(authenticationManager);
        this.authBlacklistService = authBlacklistService;
        this.userRepository = userRepository;
    }

    /**
     * Filtro que intercepta todas las peticiones y valida si existe y si es válido el token JWT.
     *
     * Si el token es válido, se extran las credenciales del usuario y se registra en el contexto de seguridad de Spring.
     *
     * Caso contrario, se interrumpe el flujo y se devuelve una respuesta con estado HTTP 401.
     * @param request petición HTTP.
     * @param response respuesta HTTP.
     * @param chain cadena de filtros de seguridad.
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        String header = request.getHeader(HEADER_AUTHORIZATION);

        //Verificar si el token viene, si no viene, pasamos al siguiente filtro o al controlador final
        if (header == null || !header.startsWith(PREFIX_TOKEN)) {
            chain.doFilter(request, response);
            return;
        }
        
        String token = header.replace(PREFIX_TOKEN, "");
        
        //Verificar si el token esta en la blacklist
        if (!checkTokenInBlacklist(token, response)) return;

        try {
            UsernamePasswordAuthenticationToken authenticationToken = getAuthenticationFromToken(token, request);
            SecurityContextHolder .getContext().setAuthentication(authenticationToken);
            chain.doFilter(request, response);
        } catch (JwtException e) {
            ResponseEntity<Response<Void>> entity = new ResponseEntity<>(
                ResponseUtils.buildErrorResponse(
                    List.of("El token JWT es inválido", e.getMessage())), 
                HttpStatus.UNAUTHORIZED);
            
            ResponseUtils.writeResponse(response, entity, CONTENT_TYPE);
        }
    }

    /**
     * Verifica si el token JWT recibido se encuentra en la blacklist.
     * @param token el JWT ya limpio (sin el prefijo "Bearer ")
     * @param response
     * @return boolean
     * @throws IOException
     */
    private boolean checkTokenInBlacklist(String token, HttpServletResponse response)
            throws IOException {
        try {
            if (authBlacklistService.isTokenBlacklisted(token)) {
                ResponseEntity<Response<Void>> entity = new ResponseEntity<>(
                        ResponseUtils.buildErrorResponse(
                                List.of("Token inválido: sesión cerrada o caducada")
                        ),
                        HttpStatus.UNAUTHORIZED
                );
                ResponseUtils.writeResponse(response, entity, CONTENT_TYPE);
                return false;
            }
        } catch (RedisConnectionFailureException ex) {
            ResponseEntity<Response<Void>> entity = new ResponseEntity<>(
                    ResponseUtils.buildErrorResponse(
                            List.of("Error de conexión con Redis")
                    ),
                    HttpStatus.SERVICE_UNAVAILABLE
            );
            ResponseUtils.writeResponse(response, entity, CONTENT_TYPE);
            return false;
        }
        return true;
    }

    /**
     * Este método deserealiza el token que recibe por parámetro y extrae el username y roles para que luego se pueda autenticar al usuario. Tambien verifica que si el token es de refresh, lo valida con su secret key
     * @param token
     * @param request petición HTTP.
     * @return UsernamePasswordAuthenticationToken para que sea validado
     * @throws IOException
     */
    private UsernamePasswordAuthenticationToken getAuthenticationFromToken(String token, HttpServletRequest request) throws IOException{
        Claims claims;
        //Verificar si la ruta es refresh, para validar el token del header con su secret key correspondiente
        //Esto lo hacemos así porque pasa por este filtro si o si
        if ("/auth/refresh".equals(request.getServletPath())) {
            claims = Jwts.parser()
                    .verifyWith(SECRET_KEY_REFRESH)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } else {
            claims = Jwts.parser()
                    .verifyWith(SECRET_KEY_ACCESS)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }

        String username = claims.getSubject();

        userRepository.findByUsernameAndDeletedAtIsNull(username)
            .orElseThrow(()-> new JwtException("El nombre de usuario del token no existe en el sistema"));

        String rawAuthorities = claims.get(JwtAuthenticationFilter.AUTHORITIES).toString();

        Collection<? extends GrantedAuthority> authorities = Arrays.asList(
                mapper.readValue(rawAuthorities.getBytes(), SimpleGrantedAuthority[].class)
        );

        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }
}
