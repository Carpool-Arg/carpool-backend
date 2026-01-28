package com.carpool.carpool.service.user.update;

import com.carpool.carpool.dto.security.token.TokenResponseDTO;
import com.carpool.carpool.dto.user.EmailRequestDTO;
import com.carpool.carpool.dto.user.UserPasswordChangeRequestDTO;
import com.carpool.carpool.dto.user.UserProfileUpdateRequestDTO;
import com.carpool.carpool.dto.user.UserResponseDTO;
import com.carpool.carpool.enums.token.TokenStateEnum;
import com.carpool.carpool.enums.token.TokenTypeEnum;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.mappers.user.UserMapper;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.model.user.token.UserToken;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.repository.user.token.UserTokenRepository;
import com.carpool.carpool.response.Response;

import com.carpool.carpool.service.email.IEmailService;
import com.carpool.carpool.service.user.UserBaseImplementation;
import com.carpool.carpool.utils.ResponseUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.carpool.carpool.utils.EmailMessageUtils.CONFIRM_EMAIL_CHANGE;
import static com.carpool.carpool.utils.EmailMessageUtils.MESSAGE_EMAIL_CHANGE;
import static com.carpool.carpool.utils.EmailMessageUtils.MESSAGE_FOOTER_EMAIL_CHANGE;
import static com.carpool.carpool.utils.EmailMessageUtils.SUBJECT_EMAIL_CHANGE;
import static com.carpool.carpool.utils.EmailMessageUtils.TITLE_GREETING;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserUpdateImplementation {

    private final UserBaseImplementation userBaseImplementation;
    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailService;
    private final UpdateTokenImplementation updateTokenImplementation;

    @Value("${redirect.email.change}")
    private String urlValidateEmail;

    public Response<UserResponseDTO> getAuthenticatedUser() {
        User loggedUser = userBaseImplementation.getAuthenticatedActiveUser();
        UserResponseDTO userResponseDTO = userMapper.convertUserToUserResponseDTO(loggedUser);
        return ResponseUtils.buildOKResponse(List.of("Usuario autenticado"), userResponseDTO);
    }

    public Response<TokenResponseDTO> updateUserProfile(UserProfileUpdateRequestDTO userProfileUpdateRequestDTO) {
        User loggedUser = userBaseImplementation.getAuthenticatedActiveUser();

        try {

            String newPhone = userProfileUpdateRequestDTO.getPhone();
            if (newPhone != null && !newPhone.trim().isEmpty() && !newPhone.equals(loggedUser.getPhone())) {
                userBaseImplementation.validateUniquePhone(newPhone);
            }

            if (userProfileUpdateRequestDTO.getGender() == null) {
                throw new ConflictException("El género no puede quedar en blanco.");
            }

            userMapper.updateUserProfileFromDTO(loggedUser, userProfileUpdateRequestDTO);
            loggedUser.setGender(userProfileUpdateRequestDTO.getGender());
            userRepository.save(loggedUser);
            
            TokenResponseDTO tokenResponseDTO = updateTokenImplementation.invalidateAllUserTokensAndGenerateNew(loggedUser);
            return ResponseUtils.buildOKResponse(List.of("Perfil actualizado correctamente."), tokenResponseDTO);

        } catch (Exception e) {
            throw new ConflictException("Error al actualizar el perfil: " + e.getMessage());
        }
    }

    public Response<TokenResponseDTO> updateUserEmail(EmailRequestDTO emailRequestDTO) {
        User loggedUser = userBaseImplementation.getAuthenticatedActiveUser();

        if (loggedUser.getEmail().equals(emailRequestDTO.getEmail())) {
            throw new ConflictException("El nuevo email no puede ser el mismo que el actual.");
        }

        if (userRepository.findByEmailAndDeletedAtIsNull(emailRequestDTO.getEmail()).isPresent()) {
            throw new ConflictException("Ya existe un usuario con el nuevo email ingresado.");
        }

        // Invalidar tokens previos del mismo tipo
        userBaseImplementation.invalidateUserTokensByType(loggedUser, TokenTypeEnum.EMAIL_CHANGE);

        
        userMapper.updateEmailFromDTO(loggedUser, emailRequestDTO.getEmail());
        userRepository.save(loggedUser);

        
        UserToken userToken = userBaseImplementation.buildUserToken(loggedUser, TokenTypeEnum.EMAIL_CHANGE);
        userTokenRepository.save(userToken);

        String confirmLink = urlValidateEmail.replace("value", userToken.getToken());
        emailService.sendEmail(
            emailRequestDTO.getEmail(),
            SUBJECT_EMAIL_CHANGE,
            TITLE_GREETING.replace("{name}", loggedUser.getName()),
            MESSAGE_EMAIL_CHANGE,
            null,
            confirmLink,
            CONFIRM_EMAIL_CHANGE,
            MESSAGE_FOOTER_EMAIL_CHANGE
        );

        TokenResponseDTO tokenResponseDTO = updateTokenImplementation.invalidateAllUserTokensAndGenerateNew(loggedUser);
        return ResponseUtils.buildOKResponse(List.of("Email actualizado correctamente. Se requiere confirmación."), tokenResponseDTO);
    }

    public Response<TokenResponseDTO> updateUserPassword(UserPasswordChangeRequestDTO passwordChangeRequestDTO) {
        User loggedUser = userBaseImplementation.getAuthenticatedActiveUser();

        if (!passwordEncoder.matches(passwordChangeRequestDTO.getOldPassword(), loggedUser.getPassword())) {
            throw new ConflictException("La contraseña actual es incorrecta.");
        }

        if (passwordEncoder.matches(passwordChangeRequestDTO.getNewPassword(), loggedUser.getPassword())) {
            throw new ConflictException("La nueva contraseña no puede ser igual a la actual.");
        }

        if (!passwordChangeRequestDTO.getNewPassword().equals(passwordChangeRequestDTO.getConfirmNewPassword())) {
            throw new ConflictException("La nueva contraseña no coincide con su confirmación.");
        }

        userMapper.updatePasswordFromDTO(loggedUser, passwordEncoder.encode(passwordChangeRequestDTO.getNewPassword()));
        userRepository.save(loggedUser);

        TokenResponseDTO tokenResponseDTO = updateTokenImplementation.invalidateAllUserTokensAndGenerateNew(loggedUser);
        return ResponseUtils.buildOKResponse(List.of("Contraseña actualizada correctamente."), tokenResponseDTO);
    }

    @Transactional
    public Response<Void> confirmEmailChange(String token) {
        UserToken tokenValidate = userTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token no encontrado"));

        if (tokenValidate.isExpired()) {
            tokenValidate.setState(TokenStateEnum.EXPIRED);
            userTokenRepository.save(tokenValidate);
            throw new ConflictException("El tiempo para la confirmación del cambio de correo ha expirado. Por favor, solicita uno nuevo.");
        }

        if (!userBaseImplementation.validateUserToken(tokenValidate, TokenTypeEnum.EMAIL_CHANGE)) {
            throw new ConflictException("Token inválido");
        }

        User user = userRepository.findById(tokenValidate.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (user.getPendingEmail() == null) {
            throw new ConflictException("No hay cambio de email pendiente");
        }

        user.setEmail(user.getPendingEmail());
        user.setPendingEmail(null);
        tokenValidate.setState(TokenStateEnum.USED);
        tokenValidate.setUsedAt(LocalDateTime.now());

        userRepository.save(user);
        userTokenRepository.save(tokenValidate);

        return ResponseUtils.buildOKResponse(List.of("Email actualizado correctamente"), null);
    }
}
