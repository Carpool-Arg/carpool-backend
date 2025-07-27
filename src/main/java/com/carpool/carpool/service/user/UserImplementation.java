package com.carpool.carpool.service.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.carpool.carpool.dto.user.UserUpdateRequestDTO;
import com.carpool.carpool.enums.token.TokenStateEnum;
import com.carpool.carpool.enums.token.TokenTypeEnum;
import com.carpool.carpool.enums.user.UserStateEnum;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.exception.UnauthorizedException;
import com.carpool.carpool.model.user.UserToken;
import com.carpool.carpool.repository.user.token.UserTokenRepository;
import com.carpool.carpool.service.email.IEmailService;
import com.carpool.carpool.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.carpool.carpool.dto.user.UserRequestDTO;
import com.carpool.carpool.mappers.user.UserMapper;
import com.carpool.carpool.model.role.Role;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.role.RoleRepository;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.utils.ResponseUtils;

import jakarta.transaction.Transactional;

@RequiredArgsConstructor
@Service
public class UserImplementation implements IUserService {

    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailImplementation;
    private final UserTokenRepository userTokenRepository;

    private static final String EXIST_USER = "Ya existe un usuario con el ";

    private static final String SUBJECT_EMAIL = "Activación de cuenta";
    private static final String TITLE = "¡Casi listo, {name}!\uD83D\uDE4C";
    private static final String MESSAGE_EMAIL = "Haz clic en el botón de abajo para activar tu cuenta:";
    private static final String CONFIRM = "Activar cuenta";
    private static final String MESSAGE_FOOTER = "Si no solicitaste esta activación, podés ignorar este correo. Recuerda que el mismo es válido durante <strong>48 horas</strong>.";

    public static final String ROLE_USER = "ROLE_USER";

    @Value("${redirect.validate.email}")
    private String urlValidateEmail;

    @Override
    @Transactional
    public Response<Void> saveUser(UserRequestDTO userRequestDTO) {

        passwordsMatch(userRequestDTO.getPassword(), userRequestDTO.getConfirmPassword());
        existsByUsername(userRequestDTO.getUsername());
        existsByEmail(userRequestDTO.getEmail());
        existsByDni(userRequestDTO.getDni());
        validateUniquePhone(userRequestDTO.getPhone());

        Optional<Role> optionalRoleUser = roleRepository.findByName(ROLE_USER);
        List<Role> roles = new ArrayList<>();
        optionalRoleUser.ifPresent(roles::add);

        User user = userMapper.convertUserRequestDTOToUser(
            userRequestDTO,
            passwordEncoder.encode(userRequestDTO.getPassword()),
            roles);
        userRepository.save(user);

        saveRequestActivationAccount(user);

        return ResponseUtils.buildOKResponse(List.of("Usuario creado") , null);
    }

    @Override
    @Transactional
    public Response<Void> updateUser(UserUpdateRequestDTO userUpdateRequestDTO) {

        User user = getUserByEmail(userUpdateRequestDTO.getEmail());

        if(!user.getStatus().equals(UserStateEnum.PENDING_PROFILE)) throw new UnauthorizedException("El usuario no tiene un registro pendiente para completar.");

        passwordsMatch(userUpdateRequestDTO.getPassword(), userUpdateRequestDTO.getConfirmPassword());
        existsByUsername(userUpdateRequestDTO.getUsername());
        existsByDni(userUpdateRequestDTO.getDni());

        Optional<Role> optionalRoleUser = roleRepository.findByName(ROLE_USER);
        List<Role> roles = new ArrayList<>();
        optionalRoleUser.ifPresent(roles::add);
        user = userMapper.convertUserUpdateRequestDTOToUser(
                user,
                userUpdateRequestDTO,
                passwordEncoder.encode(userUpdateRequestDTO.getPassword()),
                roles);
        userRepository.save(user);

        saveRequestActivationAccount(user);

        return ResponseUtils.buildOKResponse(List.of("Usuario con registro parcial creado") , null);
    }

    @Override
    @Transactional
    public Response<Void> activateAccount(String token){
        UserToken tokenValidate = userTokenRepository.findByToken(token).
                orElseThrow(() -> new ResourceNotFoundException("Token no encontrado"));

        if(!tokenValidate.getState().equals(TokenStateEnum.PENDING) || !tokenValidate.getType().equals(TokenTypeEnum.ACTIVATION)){
            throw new ConflictException("El token ya expiró");
        }
        if(!tokenValidate.getToken().equals(token)){
            throw new ConflictException("El token es inválido");
        }

        User user = userRepository.findById(tokenValidate.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        user.setStatus(UserStateEnum.ACTIVE);
        tokenValidate.setState(TokenStateEnum.USED);
        tokenValidate.setUsedAt(LocalDateTime.now());

        userRepository.save(user);
        userTokenRepository.save(tokenValidate);

        return ResponseUtils.buildOKResponse(List.of("Usuario activado con éxito") , null);
    }

    @Override
    public Response<Void> resendActivateAccount(String email) {
        Optional<User> user = userRepository.findByEmailAndDeletedAtIsNull(email);
        if(user.isPresent() && user.get().getStatus() == UserStateEnum.PENDING_VERIFICATION){
            saveRequestActivationAccount(user.get());
        }
        return ResponseUtils.buildOKResponse(List.of("Notificación enviada con éxito") , null);
    }

    @Override
    public Response<Void> validateUsername(String username){
        existsByUsername(username);
        return ResponseUtils.buildOKResponse(List.of("Nombre de usuario disponible") , null);
    }

    @Override
    public Response<Void> validateEmail(String email) {
        existsByEmail(email);
        return ResponseUtils.buildOKResponse(List.of("Email disponible") , null);
    }

    @Override
    public Response<Void> validateDni(String dni) {
        existsByDni(dni);
        return ResponseUtils.buildOKResponse(List.of("DNI disponible") , null);
    }

    /**
     * Metodo para comprobar que las contraseña y la confirmacion de la misma coinciden
     * @param userPassword la contraseña del usuario
     * @param userConfirmPassword la confirmacion de la contraseña del usuario
     * @throws ConflictException si no coinciden
     */
    private void passwordsMatch(String userPassword, String userConfirmPassword){
        if(!userPassword.equals(userConfirmPassword)){
            throw new ConflictException("Las contraseñas ingresadas no coinciden");
        }
    }
    
    /**
     * Metodo para comprobar no exista otro usuario en la base de datos
     * con el mismo email que el ingresado 
     * @param email el email ingresado por el usuario
     * @throws ConflictException si hay un usuario registrado con este email
     */
    private void existsByEmail(String email){
        userRepository.findByEmailAndDeletedAtIsNull(email).ifPresent(user -> {
            throw new IllegalArgumentException(EXIST_USER.concat("correo electrónico ingresado."));
        });
    }

    /**
     * Meotodo para comprobar que exista un usuario en la base de datos
     * con el correo electronico recibido de {@link UserUpdateRequestDTO}
     * @param email el email del usuario
     * @throws {@link ResourceNotFoundException} si no hay un usuario con el email
     */
    private User getUserByEmail(String email){
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el correo: " + email));
    }

    /**
     * Meotodo para comprobar que no exista otro usuario en la base de datos 
     * con el mismo nombre de usuario que el ingresado
     * @param username el nombre de usuario ingresado
     * @throws ConflictException si hay un usuario con este nombre de usuario
     */
    private void existsByUsername(String username){
        userRepository.findByUsernameAndDeletedAtIsNull(username).ifPresent(user -> {
            throw new ConflictException(EXIST_USER.concat("nombre de usuario ingresado."));
        });
    } 

    /**
     * Metodo para comprobar que no exista otro usuario en la base de datos
     *  con el mismo dni que el ingresado
     * @param dni dni ingresado por el usuario
     * @throws ConflictException si hay un usuario registrado con este dni
     */
    private void existsByDni(String dni){
        userRepository.findByDniAndDeletedAtIsNull(dni).ifPresent(user -> {
            throw new ConflictException(EXIST_USER.concat("DNI ingresado."));
        });
    }

    /**
     * Metodo para comprobar que no exista otro usuario en la base de datos
     * con el mismo número de teléfono que el ingresado
     * @param phone el número de teléfono ingresado por el usuario
     * @throws ConflictException si hay un usuario registrado con este número de teléfono
     */

    private void validateUniquePhone(String phone){
        userRepository.findByPhoneAndDeletedAtIsNull(phone).ifPresent(user -> {
            throw new ConflictException(EXIST_USER.concat("número de teléfono ingresado."));
        } );
    }

    /**
     * Metodo que se encarga de almacenar una solicitud para activar la cuenta del usuario en la base de datos.
     * @param user Objeto del tipo {@link User}
     */
    private void saveRequestActivationAccount(User user){
        UserToken userToken = buildUserToken(user);
        userTokenRepository.save(userToken);
        emailImplementation.sendEmail(user.getEmail(), SUBJECT_EMAIL, TITLE.replace("{name}", user.getName()), MESSAGE_EMAIL, null,urlValidateEmail.replace("value", userToken.getToken()), CONFIRM, MESSAGE_FOOTER);
    }

    /**
     * Metodo encargado de crear un objeto {@link UserToken}
     * @return Objeto {@link UserToken}
     */
    private UserToken buildUserToken(User user){
        String token = TokenUtils.generateToken(userTokenRepository);
        return UserToken.builder()
                .token(token)
                .type(TokenTypeEnum.ACTIVATION)
                .state(TokenStateEnum.PENDING)
                .user(user)
                .build();
    }
}
