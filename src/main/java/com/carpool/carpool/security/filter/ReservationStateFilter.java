package com.carpool.carpool.security.filter;

import com.carpool.carpool.model.reservation.Reservation;
import com.carpool.carpool.repository.reservation.ReservationRepository;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.security.model.AllowedEndpoint;
import com.carpool.carpool.utils.ResponseUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import static com.carpool.carpool.security.config.TokenJwtConfig.SECRET_KEY_ACCESS;

@Slf4j
public class ReservationStateFilter extends OncePerRequestFilter {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    /**
     * Endpoints permitidos para usuarios con reservas UNPAID o EXPIRED
     * (comunes a ambos estados)
     */
    private static final Set<AllowedEndpoint> COMMON_ALLOWED_ENDPOINTS = Set.of(
            new AllowedEndpoint("/login", Set.of(HttpMethod.POST)),
            new AllowedEndpoint("/auth/logout", Set.of(HttpMethod.POST, HttpMethod.DELETE)),
            new AllowedEndpoint("/auth/verify-token", Set.of(HttpMethod.GET)),
            new AllowedEndpoint("/user/debtor", Set.of(HttpMethod.GET)),
            new AllowedEndpoint("/users", Set.of(HttpMethod.GET), true)
    );

    /**
     * Endpoints permitidos SOLO cuando la reserva está UNPAID
     */
    private static final Set<AllowedEndpoint> UNPAID_ONLY_ENDPOINTS = Set.of(
            new AllowedEndpoint("/reservation/payment", Set.of(HttpMethod.POST))
    );

    public ReservationStateFilter(ReservationRepository reservationRepository,
                                  UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.replace("Bearer ", "");

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(SECRET_KEY_ACCESS)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String username = claims.getSubject();

            var userOptional = userRepository.findByUsername(username);
            if (userOptional.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }

            Long userId = userOptional.get().getId();

            String requestPath = getPathWithoutContextPath(request);
            HttpMethod requestMethod = HttpMethod.valueOf(request.getMethod());

            log.debug("Validando acceso para usuario {}: {} {}", username, requestMethod, requestPath);

            Optional<Reservation> unpaidReservation =
                    reservationRepository.findUnpaidReservationByUserId(userId);

            Optional<Reservation> expiredReservation =
                    reservationRepository.findExpiredReservationByUserId(userId);

            // ---------------- UNPAID ----------------
            if (unpaidReservation.isPresent()) {
                if (!isAllowedForUnpaid(requestPath, requestMethod)) {
                    log.warn("Usuario {} con reserva UNPAID intentó acceder a {} {}",
                            username, requestMethod, requestPath);
                    sendRestrictedAccessResponse(
                            response, "UNPAID"
                    );
                    return;
                }
            }

            // ---------------- EXPIRED ----------------
            if (expiredReservation.isPresent()) {
                if (!isAllowedForExpired(requestPath, requestMethod)) {
                    log.warn("Usuario {} con reserva EXPIRED intentó acceder a {} {}",
                            username, requestMethod, requestPath);
                    sendRestrictedAccessResponse(
                            response, "EXPIRED"
                    );
                    return;
                }
            }

            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            log.error("Error al validar el token JWT: {}", e.getMessage());
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Obtiene el path de la request sin el context path.
     * Ej: /carpool/api/v1/users -> /users
     */
    private String getPathWithoutContextPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestURI = request.getRequestURI();

        if (contextPath != null && !contextPath.isEmpty()
                && requestURI.startsWith(contextPath)) {
            return requestURI.substring(contextPath.length());
        }

        return requestURI;
    }

    /**
     * UNPAID comunes + pago
     */
    private boolean isAllowedForUnpaid(String path, HttpMethod method) {
        return Stream.concat(
                COMMON_ALLOWED_ENDPOINTS.stream(),
                UNPAID_ONLY_ENDPOINTS.stream()
        ).anyMatch(endpoint -> endpoint.matches(path, method));
    }

    /**
     * EXPIRED  solo comunes
     */
    private boolean isAllowedForExpired(String path, HttpMethod method) {
        return COMMON_ALLOWED_ENDPOINTS.stream()
                .anyMatch(endpoint -> endpoint.matches(path, method));
    }

    /**
     * Respuesta de acceso restringido
     */
    private void sendRestrictedAccessResponse(HttpServletResponse response,
                                              String reservationState) throws IOException {

        String message;

        switch (reservationState) {
            case "UNPAID" -> message =
                    "Tienes una reserva pendiente de pago. Debes completar el pago antes de acceder a otras funcionalidades.";
            case "EXPIRED" -> message =
                    "Tu cuenta fue bloqueada temporalmente debido al vencimiento del pago de una reserva. " +
                            "Esta situación se considera una falta según las condiciones de uso.";
            default -> message =
                    "No puedes acceder a esta funcionalidad debido al estado actual de tu reserva.";
        }

        var errorResponse = ResponseUtils.buildErrorResponse(List.of(message));

        ResponseEntity<?> entity = ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorResponse);

        ResponseUtils.writeResponse(response, entity, "application/json");
    }
}
