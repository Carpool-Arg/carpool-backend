package com.carpool.carpool.service.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.carpool.carpool.dto.user.UserUpdateRequestDTO;
import com.carpool.carpool.enums.media.CategoryMediaEnum;
import com.carpool.carpool.enums.token.TokenStateEnum;
import com.carpool.carpool.enums.token.TokenTypeEnum;
import com.carpool.carpool.enums.user.UserStateEnum;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.exception.UnauthorizedException;
import com.carpool.carpool.model.user.UserToken;
import com.carpool.carpool.repository.user.token.UserTokenRepository;
import com.carpool.carpool.service.auth.blacklist.IAuthBlacklistService;
import com.carpool.carpool.service.email.IEmailService;
import com.carpool.carpool.utils.PasswordUtils;
import com.carpool.carpool.service.media.IMediaService;
import com.carpool.carpool.utils.TokenUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.carpool.carpool.dto.security.token.TokenResponseDTO;
import com.carpool.carpool.dto.user.UserPasswordChangeRequestDTO;
import com.carpool.carpool.dto.user.UserProfileUpdateRequestDTO;
import com.carpool.carpool.dto.user.ChangePasswordRequestDTO;
import com.carpool.carpool.dto.user.EmailRequestDTO;
import com.carpool.carpool.dto.user.UserRequestDTO;
import com.carpool.carpool.dto.user.UserResponseDTO;
import com.carpool.carpool.mappers.user.UserMapper;
import com.carpool.carpool.model.media.Media;
import com.carpool.carpool.model.role.Role;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.media.MediaRepository;
import com.carpool.carpool.repository.role.RoleRepository;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.security.model.CustomUserDetails;
import com.carpool.carpool.security.utils.JwtUtils;
import com.carpool.carpool.utils.ResponseUtils;

import jakarta.servlet.http.HttpServletRequest;
import static com.carpool.carpool.utils.EmailMessageUtils.*;
import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class UserImplementation implements IUserService {

    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailImplementation;
    private final UserTokenRepository userTokenRepository;
    private final IMediaService mediaService;
    private final MediaRepository mediaRepository;

    private static final String EXIST_USER = "Ya existe un usuario con el ";

    public static final String ROLE_USER = "ROLE_USER";

    //Constantes para los claims del JWT
    public final static String AUTHORITIES_CLAIM = "authorities";
    private final IAuthBlacklistService authBlacklistService;
    private final HttpServletRequest  request;

    //Mensaje generico para el envío de correo electronico para solicitar el cambio de contraseña
    private static final List<String> SENDED_PASSWORD_CHANGE_EMAIL_MESSAGE = List.of("Correo enviado exitosamente.");

    @Value("${redirect.validate.email}")
    private String urlValidateEmail;

    @Value("${redirect.password.change}")
    private String urlChangePassword;

    @Value("${cloudflare.r2.bucket-public}")
    private String nameBucketPublic;


    @Override
    @Transactional
    public Response<Void> saveUser(UserRequestDTO userRequestDTO) {

        PasswordUtils.passwordsMatch(userRequestDTO.getPassword(), userRequestDTO.getConfirmPassword());
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

        Media media = buildMedia(user, nameBucketPublic, CategoryMediaEnum.PROFILE, "default-profile.png", "default-profile.png", "image/png", 4720L);
        mediaRepository.save(media);

        saveRequestActivationAccount(user);

        return ResponseUtils.buildOKResponse(List.of("Usuario creado") , null);
    }

    @Override
    @Transactional
    public Response<Void> updateUser(UserUpdateRequestDTO userUpdateRequestDTO) {

        User user = getUserByEmail(userUpdateRequestDTO.getEmail());

        if(!user.getStatus().equals(UserStateEnum.PENDING_PROFILE)) throw new UnauthorizedException("El usuario no tiene un registro pendiente para completar.");

        PasswordUtils.passwordsMatch(userUpdateRequestDTO.getPassword(), userUpdateRequestDTO.getConfirmPassword());
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


    /**
    *    Metodo encargado de crear un objeto {@link Media}
    *    Este objeto se utiliza para almacenar la informacion del archivo subido por el usuario.
    *
    *    @param user Propietario del tipo {@link User}
    *    @param bucket Nombre del bucket del tipo {@link String}
    *    @param objectKey Identificador unico del archivo en R2 del tipo {@link String}
    *    @param filename Nombre del archivo {@link String}
    *    @param contentType Tipo de contenido del archivo del tipo {@link String}
    *    @param byteSize Tamanio del archivo del tipo {@link Long}
    *    @return Objeto {@link Media}
    */
    private Media buildMedia(User user, String bucket, CategoryMediaEnum category, String objectKey, String filename, String contentType, Long byteSize) {
        Media media = new Media();
        media.setUser(user);
        media.setBucket(bucket);
        media.setCategory(category);
        media.setObjectKey(objectKey);
        media.setFileName(filename);
        media.setContentType(contentType);
        media.setByteSize(byteSize);
        media.setCreatedAt(LocalDateTime.now());

        return media;
    }

    /**
     * Metodo para traer todos los datos del usuario autenticado.
     * @return Response<UserResponseDTO> con los datos del usuario autenticado.
     */
    @Override
    public Response<UserResponseDTO> getAuthenticatedUser() {
        User loggedUser = getAuthenticatedActiveUser();
        UserResponseDTO userResponseDTO = userMapper.convertUserToUserResponseDTO(loggedUser);
        return ResponseUtils.buildOKResponse(List.of("Usuario autenticado"), userResponseDTO);
    }


    @Override
    public Response<TokenResponseDTO> updateUserProfile(UserProfileUpdateRequestDTO userProfileUpdateRequestDTO, MultipartFile profileImage) {

        User loggedUser = getAuthenticatedActiveUser();

        try {

            String newPhone = userProfileUpdateRequestDTO.getPhone();
            if (newPhone != null && !newPhone.trim().isEmpty() && !newPhone.equals(loggedUser.getPhone())) {
                validateUniquePhone(newPhone);
            }

            if (userProfileUpdateRequestDTO.getGender() == null) {
                throw new ConflictException("El género no puede quedar en blanco.");
            }

            mediaService.uploadAndSaveFileUser(profileImage, loggedUser.getId());
            userMapper.updateUserProfileFromDTO(loggedUser, userProfileUpdateRequestDTO);
            loggedUser.setGender(userProfileUpdateRequestDTO.getGender());
            userRepository.save(loggedUser);

            TokenResponseDTO tokenResponseDTO = invalidateAllUserTokensAndGenerateNew(loggedUser);
            return ResponseUtils.buildOKResponse(List.of("Perfil actualizado correctamente."), tokenResponseDTO);

        } catch (Exception e) {
            throw new ConflictException("Error al actualizar el perfil: " + e.getMessage());
        }
    }

    @Override
    public Response<TokenResponseDTO> updateUserEmail(EmailRequestDTO emailRequestDTO) {

        User loggedUser = getAuthenticatedActiveUser();

        if (loggedUser.getEmail().equals(emailRequestDTO.getEmail())) {
            throw new ConflictException("El nuevo email no puede ser el mismo que el actual.");
        }

        if (userRepository.findByEmailAndDeletedAtIsNull(emailRequestDTO.getEmail()).isPresent()) {
            throw new ConflictException("Ya existe un usuario con el nuevo email ingresado.");
        }

        invalidateUserTokensByType(loggedUser, TokenTypeEnum.EMAIL_CHANGE);

        userMapper.updateEmailFromDTO(loggedUser, emailRequestDTO.getEmail());
        userRepository.save(loggedUser);

        UserToken loggeduserToken = buildUserToken(loggedUser, TokenTypeEnum.EMAIL_CHANGE);
        userTokenRepository.save(loggeduserToken);

        String confirmLink = urlValidateEmail.replace("value", loggeduserToken.getToken());

        // Enviamos el email de confirmación al nuevo correo
        emailImplementation.sendEmail(
                emailRequestDTO.getEmail(),
                "Confirmación de cambio de correo",
                "¡Hola " + loggedUser.getName() + "!",
                "Hacé clic en el siguiente botón para confirmar tu nuevo correo electrónico:",
                null,
                confirmLink,
                "Confirmar nuevo correo",
                "Si no solicitaste este cambio, ignorá este mensaje."
        );

        TokenResponseDTO tokenResponseDTO = invalidateAllUserTokensAndGenerateNew(loggedUser);
        return ResponseUtils.buildOKResponse(List.of("Email actualizado correctamente. Se requiere confirmación."), tokenResponseDTO);
    }


    @Override
    public Response<TokenResponseDTO> updateUserPassword(UserPasswordChangeRequestDTO passwordChangeRequestDTO) {

        User loggedUser = getAuthenticatedActiveUser();

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


        TokenResponseDTO tokenResponseDTO = invalidateAllUserTokensAndGenerateNew(loggedUser);
        return ResponseUtils.buildOKResponse(List.of("Contraseña actualizada correctamente."), tokenResponseDTO);
    }

    @Override
    @Transactional
    public Response<Void> confirmEmailChange(String token) {
        UserToken tokenValidate = userTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token no encontrado"));


        if (tokenValidate.isExpired()) {
            tokenValidate.setState(TokenStateEnum.EXPIRED);
            userTokenRepository.save(tokenValidate);
            throw new ConflictException("El tiempo para la confirmacón del cambio de correo ha expirado. Por favor, solicita uno nuevo.");
        }

        if (!validateUserToken(tokenValidate,TokenTypeEnum.EMAIL_CHANGE)) throw new ConflictException("Token inválido");

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
        UserToken userToken = TokenUtils.buildUserToken(user, TokenTypeEnum.ACTIVATION, userTokenRepository);
        userTokenRepository.save(userToken);
        emailImplementation.sendEmail(user.getEmail(), SUBJECT_EMAIL_ACTIVE, TITLE_ACTIVE.replace("{name}", user.getName()), MESSAGE_EMAIL_ACTIVE, null,urlValidateEmail.replace("value", userToken.getToken()), ACTIVE, MESSAGE_FOOTER_ACTIVE);
    }

    /**
     * Metodo encargado de crear un objeto {@link UserToken}
     * @param type el tipo del objeto {@link UserToken} que vamos a crear
     * @return Objeto {@link UserToken}
     */
    private UserToken buildUserToken(User user, TokenTypeEnum type){
        String token = TokenUtils.generateToken(userTokenRepository);
        return UserToken.builder()
                .token(token)
                .type(type)
                .state(TokenStateEnum.PENDING)
                .user(user)
                .build();
    }

    /**
     * Metodo para obtener el usuario autenticado actualmente.
     * @return El usuario autenticado del tipo {@link User}
     */
    private  User getAuthenticatedActiveUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
    }

    /**
     * Metodo que obtiene tanto el access token como el refresh token de la request actual
     * @return Array con [accessToken, refreshToken]
     */
    private String[] getCurrentTokens() {
        String accessToken = null;
        String refreshToken = null;

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        refreshToken = request.getHeader("X-Refresh-Token");

        if (accessToken == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getCredentials() instanceof String) {
                accessToken = (String) authentication.getCredentials();
            }
        }

        return new String[]{accessToken, refreshToken};
    }

    /**
     * Metodo para generar nuevos tokens para el usuario autenticado y los invalida en la blacklist.
     * @param user El usuario para el cual se generarán los nuevos tokens.}
     * @return Un objeto {@link TokenResponseDTO} que contiene el nuevo access token y refresh token.
     */
    @Transactional
    public TokenResponseDTO invalidateAllUserTokensAndGenerateNew(User user) {
        try {
            String[] currentTokens = getCurrentTokens();
            String currentAccessToken = currentTokens[0];
            String currentRefreshToken = currentTokens[1];

            if (currentAccessToken != null) {
                try {
                    authBlacklistService.blacklistAccessTokenOnly(currentAccessToken);
                } catch (Exception e) {
                   throw new ConflictException("Error al invalidar el access token: " + e.getMessage());
                }
            }

            if (currentRefreshToken != null) {
                try {
                    authBlacklistService.blacklistRefreshToken(currentRefreshToken);
                } catch (Exception e) {
                    throw new ConflictException("Error al invalidar el refresh token: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            throw new ConflictException("Error al invalidar los tokens: " + e.getMessage());
        }

        return generateTokensForUser(user);
    }


    /**
     * Genera los tokens JWT para un usuario dado.
     * Este método crea un objeto {@link CustomUserDetails} a partir del usuario,
     * serializa sus autoridades a JSON, y genera un token de acceso y uno de actualización.
     * @param user
     * @return Un objeto {@link TokenResponseDTO} que contiene el token de acceso y el token de actualización.
     */
    public TokenResponseDTO generateTokensForUser(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);

        String authoritiesJson;
        try {
            authoritiesJson = new ObjectMapper().writeValueAsString(userDetails.getAuthorities());
        } catch (JsonProcessingException e) {
            throw new ConflictException("Error al serializar autoridades para JWT: " + e.getMessage());
        }

        Claims claims = Jwts.claims()
                .add("authorities", authoritiesJson)
                .add("username", userDetails.getUsername())
                .build();

        String accessToken = JwtUtils.generateAccessToken(userDetails.getUsername(), claims);
        String refreshToken = JwtUtils.generateRefreshToken(userDetails.getUsername(), claims);

        return new TokenResponseDTO(accessToken, refreshToken);
    }

    /**
     * Invalida todos los tokens activos de un tipo específico para un usuario
     */
    private void invalidateUserTokensByType(User user, TokenTypeEnum tokenType) {
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



    /* -------------------------------------------------------------------------- */
    /*                      Solicitud de cambio de contraseña                     */
    /* -------------------------------------------------------------------------- */

    /**
     * En este metodo se realiza el envio de correo electronico con la solicitud de cambio de contraseña
     * La respuesta que recibe el frontend es la misma si se logra enviar el correo o no, para no dar
     * informacion de mas al usuario.
     * En este metodo tambien se genera y se guarda el token que se utilizara para el cambio de contraseña
     */
    @Override
    @Transactional
    public Response<Void> sendPasswordChangeEmail(EmailRequestDTO emailRequestDTO) {
        Optional<User> optionalUser = userRepository.findByEmailAndDeletedAtIsNull(emailRequestDTO.getEmail());
        if(!optionalUser.isPresent()) return sendEmailResponse();
        User user = optionalUser.get();
        if(!validateUserStatus(user)) return sendEmailResponse();
        saveChangePasswordToken(user);
        return ResponseUtils.buildOKResponse(SENDED_PASSWORD_CHANGE_EMAIL_MESSAGE, null);
    }

    /**
     * En este metodo se realiza el cambio de contraseña propiamente dicho. Se realiza la validacion para la
     * coincidencia de las contraseñas ingresadas y la validacion del token.
     * Tambien se realiza el encriptado de la contraseña
     */
    @Override
    @Transactional
    public Response<Void> changePassword(ChangePasswordRequestDTO changePasswordRequestDTO){
        String token = changePasswordRequestDTO.getToken();
        UserToken userToken = userTokenRepository.findByToken(token).
        orElseThrow(()->new ResourceNotFoundException("Token no encontrado"));

        if (!validateUserToken(userToken,TokenTypeEnum.PASSWORD_CHANGE)) throw new ConflictException("Token inválido");

        /*
         * En esta parte se verifica que el token no este expirado, si es asi se le
         * setea el estado correspondiente y se lanza una excepcion
         */
        if (userToken.isExpired()){
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
     * Metodo para validar el token de cambio de contraseña
     * Se valida el estado, el tipo y si coincide con el token que pasa el usuario como parametro
     * @param userToken el token de la base de datos
     * @param type el tipo de token que posee el token para validar
     * @return {@code true} si el token es valido, {@code false} si no
     */
    private boolean validateUserToken(UserToken userToken, TokenTypeEnum type){
        return ( userToken.getState().equals(TokenStateEnum.PENDING) &&
        userToken.getType().equals(type));
    }

    /**
     * Meotodo para validar el estado de la cuenta del usuario antes de realizar el cambio de contraseña
     * @param user Usuario que solicita el cambio
     * @return {@code true} si la cuenta esta acitva o suspendida, {@code false} si no
     */
    private boolean validateUserStatus(User user){
        return (user.getStatus() == UserStateEnum.ACTIVE || user.getStatus() == UserStateEnum.SUSPENDED);
    }

    /**
     * Metodo guardar el token del usuario en la base de datos y enviar el correo electronico
     * @param user Usuario que solicita el cambio de contraseña
     */
    private void saveChangePasswordToken(User user){
        UserToken userToken = buildUserToken(user, TokenTypeEnum.PASSWORD_CHANGE);
        userTokenRepository.save(userToken);
        emailImplementation.sendEmail(
            user.getEmail(),
            SUBJECT_EMAIL_CHANGE_PASSWORD,
            TITLE_CHANGE_PASSWORD.replace("{name}",user.getName()),
            MESSAGE_EMAIL_CHANGE_PASSWORD,
            null,
            urlChangePassword.replace("value", userToken.getToken()),
            CONFIRM_CHANGE_PASSWORD,
            MESSAGE_FOOTER_CHANGE_PASSWORD);
    }

    /**
     * Metodo para devolver una respuesta generica cuando se solicita el cambio de contraseña
     * @return una respuesta con un mensaje generico para indicar que el correo electronico fue enviado con éxito
     * (aunque no haya sido asi)
     */
    private Response<Void> sendEmailResponse(){
        return ResponseUtils.buildOKResponse(SENDED_PASSWORD_CHANGE_EMAIL_MESSAGE, null);
    }
}
