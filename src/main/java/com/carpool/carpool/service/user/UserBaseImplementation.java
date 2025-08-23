package com.carpool.carpool.service.user;

import com.carpool.carpool.enums.token.TokenStateEnum;
import com.carpool.carpool.enums.token.TokenTypeEnum;
import com.carpool.carpool.enums.user.UserStateEnum;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.exception.UnauthorizedException;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.model.user.UserToken;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.repository.user.token.UserTokenRepository;
import com.carpool.carpool.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserBaseImplementation {
    
    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    
    private static final String EXIST_USER = "Ya existe un usuario con el ";

    /**
     * Verifica si el usuario existe por correo electrónico.
     * Si existe, lanza una excepción de conflicto.
     * @param email
     * @return void
     * @throws ConflictException si el usuario ya existe con el correo electrónico proporcionado.
     */
    public void existsByEmail(String email) {
        userRepository.findByEmailAndDeletedAtIsNull(email).ifPresent(user -> {
            throw new ConflictException(EXIST_USER.concat("correo electrónico ingresado."));
        });
    }

    /**
     * Verifica si el nombre de usuario ya existe.
     * Si existe, lanza una excepción de conflicto.
     * @param username
     * @return void
     * @throws ConflictException si el nombre de usuario ya está en uso.
     */
    public void existsByUsername(String username) {
        userRepository.findByUsernameAndDeletedAtIsNull(username).ifPresent(user -> {
            throw new ConflictException(EXIST_USER.concat("nombre de usuario ingresado."));
        });
    }

    /**
     * Verifica si el DNI ya está en uso.
     * Si existe, lanza una excepción de conflicto.
     * @param dni
     * @return void
     * @throws ConflictException si el DNI ya está en uso por otro usuario.
     */
    public void existsByDni(String dni) {
        userRepository.findByDniAndDeletedAtIsNull(dni).ifPresent(user -> {
            throw new ConflictException(EXIST_USER.concat("DNI ingresado."));
        });
    }

    /**
     * Verifica si el número de teléfono es único.
     * Si ya existe un usuario con el mismo número de teléfono, lanza una excepción de conflicto.
     * @param phone
     * @return void
     * @throws ConflictException si ya existe un usuario con el número de teléfono ingresado.
     */
    public void validateUniquePhone(String phone) {
        userRepository.findByPhoneAndDeletedAtIsNull(phone).ifPresent(user -> {
            throw new ConflictException(EXIST_USER.concat("número de teléfono ingresado."));
        });
    }

    /**
     * Obtiene un usuario autenticado y activo.
     * Si no hay un usuario autenticado o el usuario no está activo, lanza una excepción.
     * @param email
     * @return User 
     * @throws ResourceNotFoundException si no se encuentra un usuario activo con el correo electrónico proporcionado.
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el correo: " + email));
    }

    /**
     * Obtiene el usuario autenticado actualmente.
     * Si no hay un usuario autenticado, lanza una excepción.
     * @return User
     * @throws ResourceNotFoundException si no se encuentra un usuario autenticado.
     */
    public User getAuthenticatedActiveUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
    }

    /**
     * Verifica el token de Google y devuelve el ID del token.
     * @param user
     * @param type
     * @return String
     * @throws UnauthorizedException si hay un error al verificar el token con Google.
     */
    public UserToken buildUserToken(User user, TokenTypeEnum type) {
        String token = TokenUtils.generateToken(userTokenRepository);
        return UserToken.builder()
                .token(token)
                .type(type)
                .state(TokenStateEnum.PENDING)
                .user(user)
                .build();
    }


    /**
     * Metodo para validar el token de cambio de contraseña
     * Se valida el estado, el tipo y si coincide con el token que pasa el usuario como parametro
     * @param userToken el token de la base de datos
     * @param type el tipo de token que posee el token para validar
     * @return {@code true} si el token es valido, {@code false} si no
    */
    public boolean validateUserToken(UserToken userToken, TokenTypeEnum type) {
        return (userToken.getState().equals(TokenStateEnum.PENDING) &&
                userToken.getType().equals(type));
    }

    /**
     * Invalida todos los tokens de un usuario por tipo.
     * Cambia el estado de los tokens a expirado y actualiza la fecha de uso
     * @param user
     * @param tokenType
     * @return void
     * @throws UnauthorizedException si no se encuentran tokens activos del tipo especificado.
     */
    public void invalidateUserTokensByType(User user, TokenTypeEnum tokenType) {
        List<UserToken> activeTokens = userTokenRepository.findByUserAndTypeAndState(
                user, tokenType, TokenStateEnum.PENDING);

        activeTokens.forEach(token -> {
            token.setState(TokenStateEnum.EXPIRED);
            token.setUsedAt(LocalDateTime.now());
        });

        if (!activeTokens.isEmpty()) {
            userTokenRepository.saveAll(activeTokens);
        }
    }

    /**
     * Meotodo para validar el estado de la cuenta del usuario antes de realizar el cambio de contraseña
     * @param user Usuario que solicita el cambio
     * @return {@code true} si la cuenta esta acitva o suspendida, {@code false} si no
     */
    public boolean validateUserStatus(User user) {
        return (user.getStatus() == UserStateEnum.ACTIVE || user.getStatus() == UserStateEnum.SUSPENDED);
    }
}
