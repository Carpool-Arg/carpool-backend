package com.carpool.carpool.service.user.notification;

import com.carpool.carpool.dto.user.UserTokenRequestDTO;
import com.carpool.carpool.enums.token.TokenStateEnum;
import com.carpool.carpool.enums.token.TokenTypeEnum;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.model.user.token.UserToken;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.repository.user.token.UserTokenRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.firebase.notification.FirebaseNotificationImplementation;
import com.carpool.carpool.service.firebase.notification.IFirebaseNotificationService;
import com.carpool.carpool.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserNotificationImplementation implements IUserNotificationService {
    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final IFirebaseNotificationService firebaseNotificationService;
    private static final Logger logger = LoggerFactory.getLogger(UserNotificationImplementation.class);

    @Override
    public Response<Void> register(UserTokenRequestDTO userTokenRequestDTO) {
        logger.info("USER-NOTIFICATION: comienzo de registro de token");
        //Obtener usuario autenticado
        User userAuth = this.getAuthenticatedActiveUser();

        // Verificar que el token es valido
        if (!firebaseNotificationService.sendSilentPush(userTokenRequestDTO.getToken())) {
            throw new ConflictException("El token no es válido");
        }

        // Verificar que el token no se repita
        Optional<UserToken> token = userTokenRepository.findByToken(userTokenRequestDTO.getToken());

        if (token.isPresent()) {
            throw new ConflictException("El token ingresado ya existe.");
        }

        //Almacenar el token
        UserToken userToken = UserToken.builder()
                .token(userTokenRequestDTO.getToken())
                .type(TokenTypeEnum.PUSH_NOTIFICATION)
                .state(TokenStateEnum.ACTIVE)
                .user(userAuth)
                .build();

        userTokenRepository.save(userToken);

        logger.info("USER-NOTIFICATION: token registrado={}", userToken.getToken());

        return ResponseUtils.buildOKResponse(List.of("Token registrado con éxito") , null);
    }

    @Override
    public Response<Void> deletePushNotifications() {
        // Obtener usuario autenticado
        User userAuth = this.getAuthenticatedActiveUser();

        // Borrar tokens de tipo PUSH_NOTIFICATION
        userTokenRepository.deleteByUserIdAndType(
                userAuth.getId(),
                TokenTypeEnum.PUSH_NOTIFICATION
        );

        return ResponseUtils.buildOKResponse(
                List.of("Notificaciones push eliminadas correctamente"),
                null
        );
    }

    @Override
    public Response<Boolean> hasActiveTokens() {
        User userAuth = this.getAuthenticatedActiveUser();

        List<UserToken> tokens = userTokenRepository.findByUserAndTypeAndState(
                userAuth,
                TokenTypeEnum.PUSH_NOTIFICATION,
                TokenStateEnum.ACTIVE
        );

        boolean hasActive = !tokens.isEmpty();

        return ResponseUtils.buildOKResponse(
                List.of("Estado de notificaciones obtenido"),
                hasActive
        );
    }

    /**
     * Obtiene el usuario autenticado actualmente.
     * Si no hay un usuario autenticado, lanza una excepción.
     * @return User
     * @throws ResourceNotFoundException si no se encuentra un usuario autenticado.
     */
    private User getAuthenticatedActiveUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
    }
}
