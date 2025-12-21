package com.carpool.carpool.service.user.register;

import com.carpool.carpool.dto.user.UserRequestDTO;
import com.carpool.carpool.dto.user.UserUpdateRequestDTO;
import com.carpool.carpool.enums.token.TokenTypeEnum;
import com.carpool.carpool.enums.user.UserStateEnum;
import com.carpool.carpool.exception.UnauthorizedException;
import com.carpool.carpool.mappers.user.UserMapper;
import com.carpool.carpool.model.role.Role;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.model.user.token.UserToken;
import com.carpool.carpool.repository.role.RoleRepository;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.repository.user.token.UserTokenRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.email.IEmailService;
import com.carpool.carpool.service.media.IMediaService;
import com.carpool.carpool.service.user.UserBaseImplementation;
import com.carpool.carpool.utils.PasswordUtils;
import com.carpool.carpool.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.carpool.carpool.utils.EmailMessageUtils.*;

@Service
@RequiredArgsConstructor
public class UserRegisterImplementation {
    
    private final UserBaseImplementation userBaseImplementation;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserTokenRepository userTokenRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailService;
    private final IMediaService mediaService; 

    @Value("${redirect.validate.email}")
    private String urlValidateEmail;

    @Value("${cloudflare.r2.bucket-public}")
    private String nameBucketPublic;

    public static final String ROLE_USER = "ROLE_USER";
 

    @Transactional
    public Response<Void> saveUser(UserRequestDTO userRequestDTO) {
    
        PasswordUtils.passwordsMatch(userRequestDTO.getPassword(), userRequestDTO.getConfirmPassword());
        userBaseImplementation.existsByUsername(userRequestDTO.getUsername());
        userBaseImplementation.existsByEmail(userRequestDTO.getEmail());
        userBaseImplementation.existsByDni(userRequestDTO.getDni());
        userBaseImplementation.validateUniquePhone(userRequestDTO.getPhone());
        userBaseImplementation.validateBirthDate(userRequestDTO.getBirthDate());

        Optional<Role> optionalRoleUser = roleRepository.findByName(ROLE_USER);
        List<Role> roles = new ArrayList<>();
        optionalRoleUser.ifPresent(roles::add);

        User user = userMapper.convertUserRequestDTOToUser(
                userRequestDTO,
                passwordEncoder.encode(userRequestDTO.getPassword()),
                roles);
        userRepository.save(user);
        mediaService.saveDefaultProfilePicture(user);
        saveRequestActivationAccount(user);

        return ResponseUtils.buildOKResponse(List.of("Usuario creado"), null);
    }

    @Transactional
    public Response<Void> completeRegistration(UserUpdateRequestDTO userUpdateRequestDTO) {
        User user = userBaseImplementation.getUserByEmail(userUpdateRequestDTO.getEmail());

        if (!UserStateEnum.PENDING_PROFILE.equals(user.getStatus())) {
            throw new UnauthorizedException("El usuario no tiene un registro pendiente para completar.");
        }
        
        PasswordUtils.passwordsMatch(userUpdateRequestDTO.getPassword(), userUpdateRequestDTO.getConfirmPassword());
        userBaseImplementation.existsByUsername(userUpdateRequestDTO.getUsername());
        userBaseImplementation.existsByDni(userUpdateRequestDTO.getDni());
        userBaseImplementation.validateUniquePhone(userUpdateRequestDTO.getPhone());

        Optional<Role> optionalRoleUser = roleRepository.findByName(ROLE_USER);
        List<Role> roles = new ArrayList<>();
        optionalRoleUser.ifPresent(roles::add);

        user = userMapper.convertUserUpdateRequestDTOToUser(
                user, userUpdateRequestDTO,
                passwordEncoder.encode(userUpdateRequestDTO.getPassword()),
                roles);

        userRepository.save(user);
        mediaService.saveDefaultProfilePicture(user);
        saveRequestActivationAccount(user);

        return ResponseUtils.buildOKResponse(List.of("Usuario con registro parcial creado"), null);
    }

    public Response<Void> resendActivateAccount(String email) {
        Optional<User> user = userRepository.findByEmailAndDeletedAtIsNull(email);
        if (user.isPresent() && user.get().getStatus() == UserStateEnum.PENDING_VERIFICATION) {
            saveRequestActivationAccount(user.get());
        }
        return ResponseUtils.buildOKResponse(List.of("Notificación enviada con éxito"), null);
    }
    /**
     * Guarda una solicitud de activación de cuenta para el usuario.
     * Envía un correo electrónico con un enlace de activación.
     * @param user El usuario para el cual se guarda la solicitud de activación.
     * @throws UnauthorizedException Si el usuario ya está activo o no es válido.
     */
    private void saveRequestActivationAccount(User user) {
        UserToken userToken = userBaseImplementation.buildUserToken(user, TokenTypeEnum.ACTIVATION);
        userTokenRepository.save(userToken);
        emailService.sendEmail(
                user.getEmail(),
                SUBJECT_EMAIL_ACTIVE,
                TITLE_ACTIVE.replace("{name}", user.getName()),
                MESSAGE_EMAIL_ACTIVE,
                null,
                urlValidateEmail.replace("value", userToken.getToken()),
                ACTIVE,
                MESSAGE_FOOTER_ACTIVE
        );
    }
}
