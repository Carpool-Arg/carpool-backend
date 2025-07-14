package com.carpool.carpool.security.filter;

import com.carpool.carpool.enums.user.UserStatus;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.utils.ResponseUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserStatusValidationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private static final List<String> EXCLUDED_PATHS = List.of(
            "/users",
            "/users/validate-username",
            "/users/validate-email",
            "/users/validate-dni",
            "/auth/google"
    );

    /**
     * Filter encargado de validar si el usuario se encuentra registrado y con estado {@code ACTIVE}. Este filter se ejecuta luego que el usuario haya obtenido
     * con éxito el token JWT.
     * @param request Request HTTP {@link HttpServletResponse}
     * @param response Response HTTP {@link HttpServletResponse}
     * @param filterChain Filter {@link FilterChain}
     * @throws ServletException Si ocurre un error en la ejecución del filtro
     * @throws IOException Si ocurre un error al escribir la respuesta.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getServletPath();
        if (EXCLUDED_PATHS.contains(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<User> optionalUser = userRepository.findByUsernameAndDeletedAtIsNull(username);

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                if (user.getStatus() != UserStatus.ACTIVE) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                    Response<Void> errorResponse = ResponseUtils.buildErrorResponse(
                            List.of("Debes activar tu cuenta para usar la aplicación"));

                    new ObjectMapper().writeValue(response.getWriter(), errorResponse);
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
