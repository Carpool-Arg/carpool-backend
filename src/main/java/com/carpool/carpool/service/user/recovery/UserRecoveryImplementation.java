package com.carpool.carpool.service.user.recovery;

import com.carpool.carpool.dto.user.ChangePasswordRequestDTO;
import com.carpool.carpool.dto.user.EmailRequestDTO;
import com.carpool.carpool.enums.token.TokenStateEnum;
import com.carpool.carpool.enums.token.TokenTypeEnum;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.model.user.UserToken;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.repository.user.token.UserTokenRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.email.IEmailService;
import com.carpool.carpool.service.user.UserBaseImplementation;
import com.carpool.carpool.utils.PasswordUtils;
import com.carpool.carpool.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.carpool.carpool.utils.EmailMessageUtils.*;

@Service
@RequiredArgsConstructor

public class UserRecoveryImplementation {
    private final UserBaseImplementation userBaseImplementation;
    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailService;

    @Value("${redirect.password.change}")
    private String urlChangePassword;

    private static final List<String> SENDED_PASSWORD_CHANGE_EMAIL_MESSAGE = List.of("Correo enviado exitosamente.");

    @Transactional
    public Response<Void> sendPasswordChangeEmail(EmailRequestDTO emailRequestDTO) {
        Optional<User> optionalUser = userRepository.findByEmailAndDeletedAtIsNull(emailRequestDTO.getEmail());
        if (!optionalUser.isPresent()) return sendEmailResponse();
        
        User user = optionalUser.get();
        if (!userBaseImplementation.validateUserStatus(user)) return sendEmailResponse();
        
        saveChangePasswordToken(user);
        return ResponseUtils.buildOKResponse(SENDED_PASSWORD_CHANGE_EMAIL_MESSAGE, null);
    }

    @Transactional
    public Response<Void> changePassword(ChangePasswordRequestDTO changePasswordRequestDTO) {
        String token = changePasswordRequestDTO.getToken();
        UserToken userToken = userTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token no encontrado"));

        if (!userBaseImplementation.validateUserToken(userToken, TokenTypeEnum.PASSWORD_CHANGE)) {
            throw new ConflictException("Token inválido");
        }

        if (userToken.isExpired()) {
            userToken.setState(TokenStateEnum.EXPIRED);
            userTokenRepository.save(userToken);
            throw new ConflictException("Token Expirado");
        }

        User user = userRepository.findById(userToken.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        String userPassword = changePasswordRequestDTO.getPassword();
        String userConfirmPassword = changePasswordRequestDTO.getConfirmPassword();
        PasswordUtils.passwordsMatch(userPassword, userConfirmPassword);
        user.setPassword(passwordEncoder.encode(userPassword));

        userToken.setState(TokenStateEnum.USED);
        userToken.setUsedAt(LocalDateTime.now());

        userRepository.save(user);
        userTokenRepository.save(userToken);
        
        return ResponseUtils.buildOKResponse(List.of("Contraseña actualizada."), null);
    }

    /**
     * Método que guarda un token de cambio de contraseña y envía un correo al usuario.
     * @param user Usuario al que se le enviará el correo.
     * @throws UnauthorizedException si el usuario no es válido o ya tiene un token activo.
     */
    private void saveChangePasswordToken(User user) {
        UserToken userToken = userBaseImplementation.buildUserToken(user, TokenTypeEnum.PASSWORD_CHANGE);
        userTokenRepository.save(userToken);
        emailService.sendEmail(
                user.getEmail(),
                SUBJECT_EMAIL_CHANGE_PASSWORD,
                TITLE_CHANGE_PASSWORD.replace("{name}", user.getName()),
                MESSAGE_EMAIL_CHANGE_PASSWORD,
                null,
                urlChangePassword.replace("value", userToken.getToken()),
                CONFIRM_CHANGE_PASSWORD,
                MESSAGE_FOOTER_CHANGE_PASSWORD
        );
    }

    /**
     * Método que envía una respuesta de éxito al usuario.
     * @return Response con un mensaje de éxito.
     */
    private Response<Void> sendEmailResponse() {
        return ResponseUtils.buildOKResponse(SENDED_PASSWORD_CHANGE_EMAIL_MESSAGE, null);
    }
}
