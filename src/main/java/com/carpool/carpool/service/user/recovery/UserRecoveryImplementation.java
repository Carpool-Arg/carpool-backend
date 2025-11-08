package com.carpool.carpool.service.user.recovery;

import static com.carpool.carpool.utils.EmailMessageUtils.CONFIRM_CHANGE_PASSWORD;
import static com.carpool.carpool.utils.EmailMessageUtils.MESSAGE_EMAIL_CHANGE_PASSWORD;
import static com.carpool.carpool.utils.EmailMessageUtils.MESSAGE_FOOTER_CHANGE_PASSWORD;
import static com.carpool.carpool.utils.EmailMessageUtils.SUBJECT_EMAIL_CHANGE_PASSWORD;
import static com.carpool.carpool.utils.EmailMessageUtils.TITLE_CHANGE_PASSWORD;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carpool.carpool.dto.user.ChangePasswordRequestDTO;
import com.carpool.carpool.dto.user.EmailRequestDTO;
import com.carpool.carpool.enums.token.TokenStateEnum;
import com.carpool.carpool.enums.token.TokenTypeEnum;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.model.user.token.UserToken;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.repository.user.token.UserTokenRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.email.IEmailService;
import com.carpool.carpool.service.user.UserBaseImplementation;
import com.carpool.carpool.utils.PasswordUtils;
import com.carpool.carpool.utils.ResponseUtils;

import lombok.RequiredArgsConstructor;

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
    
    /**
     * En este metodo se realiza el envio de correo electronico con la solicitud de cambio de contraseña
     * La respuesta que recibe el frontend es la misma si se logra enviar el correo o no, para no dar
     * informacion de mas al usuario.
     * En este metodo tambien se genera y se guarda el token que se utilizara para el cambio de contraseña
     */
    @Transactional
    public Response<Void> sendPasswordChangeEmail(EmailRequestDTO emailRequestDTO) {
        Optional<User> optionalUser = userRepository.findByEmailAndDeletedAtIsNull(emailRequestDTO.getEmail());
        if (!optionalUser.isPresent()) return sendEmailResponse();
        
        User user = optionalUser.get();
        if (!userBaseImplementation.validateUserStatus(user)) return sendEmailResponse();
        
        saveChangePasswordToken(user);
        return ResponseUtils.buildOKResponse(SENDED_PASSWORD_CHANGE_EMAIL_MESSAGE, null);
    }

     /**
     * En este metodo se realiza el cambio de contraseña propiamente dicho. Se realiza la validacion para la
     * coincidencia de las contraseñas ingresadas y la validacion del token.
     * Tambien se realiza el encriptado de la contraseña
     */
    @Transactional
    public Response<Void> changePassword(ChangePasswordRequestDTO changePasswordRequestDTO) {
        String token = changePasswordRequestDTO.getToken();
        UserToken userToken = userTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token no encontrado"));

        if (!userBaseImplementation.validateUserToken(userToken, TokenTypeEnum.PASSWORD_CHANGE)) {
            throw new ConflictException("Token inválido");
        }

        /*
         * En esta parte se verifica que el token no este expirado, si es asi se le
         * setea el estado correspondiente y se lanza una excepcion
         */
        if (userToken.isExpired()) {
            userToken.setState(TokenStateEnum.EXPIRED);
            userTokenRepository.save(userToken);
            throw new ConflictException("Token Expirado");
        }

        User user = userRepository.findById(userToken.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        userToken.setState(TokenStateEnum.USED);
        userToken.setUsedAt(LocalDateTime.now());
        
        String userPassword = changePasswordRequestDTO.getPassword();
        String userConfirmPassword = changePasswordRequestDTO.getConfirmPassword();
        PasswordUtils.passwordsMatch(userPassword, userConfirmPassword);
        user.setPassword(passwordEncoder.encode(userPassword));

        userRepository.save(user);
        userTokenRepository.save(userToken);
        
        return ResponseUtils.buildOKResponse(List.of("Contraseña actualizada."), null);
    }

    /**
     * Metodo guardar el token del usuario en la base de datos y enviar el correo electronico
     * @param user Usuario que solicita el cambio de contraseña
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
     * Metodo para devolver una respuesta generica cuando se solicita el cambio de contraseña
     * @return una respuesta con un mensaje generico para indicar que el correo electronico fue enviado con éxito
     * (aunque no haya sido asi)
     */
    private Response<Void> sendEmailResponse() {
        return ResponseUtils.buildOKResponse(SENDED_PASSWORD_CHANGE_EMAIL_MESSAGE, null);
    }
}
