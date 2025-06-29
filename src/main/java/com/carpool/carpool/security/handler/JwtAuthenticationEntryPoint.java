package com.carpool.carpool.security.handler;

import com.carpool.carpool.response.Response;
import com.carpool.carpool.utils.ResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.security.core.AuthenticationException;
import java.io.IOException;
import java.util.List;
import static com.carpool.carpool.security.config.TokenJwtConfig.CONTENT_TYPE;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        ResponseEntity<Response<Void>> entity = new ResponseEntity<>(
                ResponseUtils.buildErrorResponse(
                        List.of("Debe estar autenticado para realizar esta acción")
                ),
                HttpStatus.UNAUTHORIZED
        );

        ResponseUtils.writeResponse(response, entity, CONTENT_TYPE);
    }
}
