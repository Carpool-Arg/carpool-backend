package com.carpool.carpool.security.filter;

import com.carpool.carpool.dto.security.recaptcha.RecaptchaResponseDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.auth.recaptcha.IAuthRecaptchaService;
import com.carpool.carpool.utils.ResponseUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class RecaptchaFilter extends OncePerRequestFilter {
    private final IAuthRecaptchaService authRecaptchaService;

    private static final Logger LOGGER = LoggerFactory.getLogger(RecaptchaFilter.class);

    public RecaptchaFilter(IAuthRecaptchaService authRecaptchaService) {
        this.authRecaptchaService = authRecaptchaService;
    }

    /**Filtro Para verificar que el token recaptcha sea válido, comprobando el campo success y también el score
     * @param request petición HTTP.
     * @param response respuesta HTTP.
     * @param filterChain cadena de filtros de seguridad.
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            if ("POST".equalsIgnoreCase(request.getMethod()) &&
                    ("/login".equals(request.getServletPath()) || "/users".equals(request.getServletPath()))) {
                //  Obtener el recaptcha token del header
                String recaptcha = request.getHeader("recaptcha");

                // Validar que el token no sea null ni vacío
                if (recaptcha != null && !recaptcha.isBlank()) {
                    // Llamar al servicio que verifica el token con Google
                    RecaptchaResponseDTO recaptchaResponse = authRecaptchaService.validateToken(recaptcha);

                    //  Analizar la respuesta, si el success es false, o el score es menor a 0.5, el recaptcha falla
                    //  Score mas cercano a 1 es humano, mas cercano a 0 es un bot
                    if (!recaptchaResponse.getSuccess() || recaptchaResponse.getScore() < 0.5){
                        throw new BadCredentialsException("No se pudo verificar que eres un humano. Por favor, intenta nuevamente.");
                    }
                    LOGGER.info("AUTENTICACION RECAPTCHA EXITOSA: {}, recaptchaToken: {}", recaptchaResponse,recaptcha);
                }else{
                    throw new BadCredentialsException("No se pudo verificar que eres un humano. Por favor, intenta nuevamente.");
                }
            }

            //Pasar al siguiente filtro
            filterChain.doFilter(request,response);
        } catch (BadCredentialsException ex) {
            // Manejar errores relacionados a reCAPTCHA con 401
            ResponseEntity<Response<Void>> entity = new ResponseEntity<>(
                    ResponseUtils.buildErrorResponse(List.of(ex.getMessage())),
                    HttpStatus.UNAUTHORIZED);

            ResponseUtils.writeResponse(response, entity, "application/json");
        } catch (Exception ex) {
            LOGGER.info("ERROR INTERNO EN LA VALIDACION DEL RECAPTCHA", ex);

            // Manejar errores inesperados con 500
            ResponseEntity<Response<Void>> entity = new ResponseEntity<>(
                    ResponseUtils.buildErrorResponse(List.of("Error interno en la validación de Recaptcha", ex.getMessage() != null ? ex.getMessage() : "Error interno")),
                    HttpStatus.INTERNAL_SERVER_ERROR);

            ResponseUtils.writeResponse(response, entity, "application/json");
        }
    }
}
