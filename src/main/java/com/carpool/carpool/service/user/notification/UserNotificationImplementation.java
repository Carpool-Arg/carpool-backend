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
import com.carpool.carpool.service.firebase.notification.IFirebaseNotificationService;
import com.carpool.carpool.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
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

    @Override
    public Response<Void> register(UserTokenRequestDTO userTokenRequestDTO) {
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

        return ResponseUtils.buildOKResponse(List.of("Token registrado con éxito") , null);
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
