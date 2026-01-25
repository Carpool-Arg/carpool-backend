package com.carpool.carpool.service.user.debt;

import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.stateHistory.StateHistoryRepository;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDebtImplementation implements IUserDebtService{
    private final StateHistoryRepository stateHistoryRepository;
    private final UserRepository userRepository;

    public Response<Boolean> isDebtor() {
        User authUser = this.getAuthenticatedActiveUser();
        log.info("Verificando deudas del usuario= {}", authUser.getUsername());
        boolean isDebtor =  stateHistoryRepository.existsActiveDebtByUserId(authUser.getId());

        return ResponseUtils.buildOKResponse(List.of("Estado de deuda obtenido correctamente"),isDebtor);
    }

    public User getAuthenticatedActiveUser() {
        log.info("Obteniendo el usuario activo de la sesion");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
    }
}
