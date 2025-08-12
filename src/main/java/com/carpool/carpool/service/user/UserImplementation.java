package com.carpool.carpool.service.user;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
import com.carpool.carpool.service.auth.blacklist.IAuthBlacklistService;
import com.carpool.carpool.service.email.IEmailService;
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
import com.carpool.carpool.dto.user.UserEmailChangeRequestDTO;
import com.carpool.carpool.dto.user.UserPasswordChangeRequestDTO;
import com.carpool.carpool.dto.user.UserProfileUpdateRequestDTO;
import com.carpool.carpool.dto.user.UserRequestDTO;
import com.carpool.carpool.dto.user.UserResponseDTO;
import com.carpool.carpool.mappers.user.UserMapper;
import com.carpool.carpool.model.role.Role;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.role.RoleRepository;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.security.model.CustomUserDetails;
import com.carpool.carpool.security.utils.JwtUtils;
import com.carpool.carpool.utils.ResponseUtils;

import jakarta.servlet.http.HttpServletRequest;
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

    // Constante para los claims
    public final static String AUTHORITIES_CLAIM = "authorities";

    private final IAuthBlacklistService authBlacklistService;

    private final HttpServletRequest  request;

   
    @Value("${redirect.validate.email}")
    private String urlValidateEmail;

    
    @Value("${file.upload-dir}")
    private String uploadDir;

    
    @Value("${file.default-profile-image}")
    private String defaultProfileImage;

    
    @Value("${file.static-images-path}")
    private String staticImagesPath;

    

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

        user.setProfileImage(generateUniqueDefaultProfileImage(user));
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
        
        if (user.getProfileImage() == null || user.getProfileImage().isEmpty()) {
            user.setProfileImage(generateUniqueDefaultProfileImage(user));
        }
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
        String oldImageUrl = loggedUser.getProfileImage(); // Imagen actual del usuario
        String newImageUrl = oldImageUrl;

        // Try para manejar excepciones al acceder a la imagen del perfil
        try {

            String newPhone = userProfileUpdateRequestDTO.getPhone();
            if (newPhone != null && !newPhone.trim().isEmpty() && !newPhone.equals(loggedUser.getPhone())) {
                validateUniquePhone(newPhone);
            }
            
            if (userProfileUpdateRequestDTO.getGender() == null) {
                throw new ConflictException("El género no puede quedar en blanco.");
            }


            // Control de la imagen de perfil
            if (userProfileUpdateRequestDTO.isRemoveProfileImage()) {

                if (isDefaultProfileImage(oldImageUrl)) {
                    throw new ConflictException("No se puede eliminar la imagen de perfil por defecto.");
                }

                deleteProfileImage(oldImageUrl); // Elimina la imagen actual del sistema de archivos
                newImageUrl = getDefaultProfileImagePath(); // Asigna la imagen por defecto

            } else if (profileImage != null && !profileImage.isEmpty()) {

                if (!isValidImageType(profileImage.getContentType())) {
                    throw new ConflictException("El tipo de archivo de la imagen no es válido. Los tipos permitidos son JPEG, PNG y JPG.");
                }

                if (profileImage.getSize() > 5 * 1024 * 1024) { 
                    throw new ConflictException("El tamaño de la imagen no puede ser mayor a 5 MB.");
                }

                if (oldImageUrl != null && !isDefaultProfileImage(oldImageUrl)) {
                    deleteProfileImage(oldImageUrl);
                }
                newImageUrl = saveProfileImage(profileImage, loggedUser);
            }

            userMapper.updateUserProfileFromDTO(loggedUser, userProfileUpdateRequestDTO, newImageUrl);
            loggedUser.setGender(userProfileUpdateRequestDTO.getGender());
            userRepository.save(loggedUser);

            TokenResponseDTO tokenResponseDTO = invalidateAllUserTokensAndGenerateNew(loggedUser);
            return ResponseUtils.buildOKResponse(List.of("Perfil actualizado correctamente."), tokenResponseDTO);

        } catch (Exception e) {

            // Si ocurre un error, volvemos a la imagen anterior
            if (newImageUrl != null && !newImageUrl.equals(oldImageUrl) && !isDefaultProfileImage(newImageUrl)) {
                deleteProfileImage(newImageUrl);
            }
            throw new ConflictException("Error al actualizar el perfil: " + e.getMessage());
        }
    }
    
    @Override
    public Response<TokenResponseDTO> updateUserEmail(UserEmailChangeRequestDTO userEmailChangeRequestDTO) {
        
        User loggedUser = getAuthenticatedActiveUser();

        if (loggedUser.getEmail().equals(userEmailChangeRequestDTO.getNewEmail())) {
            throw new ConflictException("El nuevo email no puede ser el mismo que el actual.");
        }

        if (userRepository.findByEmailAndDeletedAtIsNull(userEmailChangeRequestDTO.getNewEmail()).isPresent()) {
            throw new ConflictException("Ya existe un usuario con el nuevo email ingresado.");
        }

        invalidateUserTokensByType(loggedUser, TokenTypeEnum.EMAIL_CHANGE);

        userMapper.updateEmailFromDTO(loggedUser, userEmailChangeRequestDTO.getNewEmail());
        userRepository.save(loggedUser);

        UserToken loggeduserToken = buildUserToken(loggedUser, TokenTypeEnum.EMAIL_CHANGE);
        userTokenRepository.save(loggeduserToken);

        String confirmLink = urlValidateEmail.replace("value", loggeduserToken.getToken());

        // Enviamos el email de confirmación al nuevo correo
        emailImplementation.sendEmail(
                userEmailChangeRequestDTO.getNewEmail(),
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

        if (tokenValidate.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ConflictException("El tiempo para la confirmacón del cambio de correo ha expirado. Por favor, solicita uno nuevo.");
        }
        
        if (!tokenValidate.getState().equals(TokenStateEnum.PENDING) || 
            !tokenValidate.getType().equals(TokenTypeEnum.EMAIL_CHANGE)) {
            throw new ConflictException("El token ya expiró o es inválido");
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
        UserToken userToken = buildUserToken(user, TokenTypeEnum.ACTIVATION);
        userTokenRepository.save(userToken);
        emailImplementation.sendEmail(user.getEmail(), SUBJECT_EMAIL, TITLE.replace("{name}", user.getName()), MESSAGE_EMAIL, null,urlValidateEmail.replace("value", userToken.getToken()), CONFIRM, MESSAGE_FOOTER);
    }

    /**
     * Metodo encargado de crear un objeto {@link UserToken}
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
     * Retorna la ruta de la imagen por defecto
     * @return String con la ruta de la imagen por defecto
     */
    private String getDefaultProfileImagePath() {
        return staticImagesPath + "/" + defaultProfileImage;
    }

    /**
     * Genera una copia de la imagen de perfil por defecto con un nombre único.
     * La imagen se guarda en el directorio de subidas del sistema.
     *
     * @param user El objeto User con el nombre de usuario y el rol.
     * @return La ruta relativa de la nueva imagen de perfil.
     */
    private String generateUniqueDefaultProfileImage(User user) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path defaultImagePath = Paths.get(uploadDir, defaultProfileImage);
            if (!Files.exists(defaultImagePath)) {
                throw new ConflictException("La imagen de perfil por defecto no se encuentra en el servidor.");
            }

            String originalFilename = defaultProfileImage;
            String fileExtension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String newFileName = user.getUsername() + fileExtension;
            
            Path newImagePath = uploadPath.resolve(newFileName);
            Files.copy(defaultImagePath, newImagePath);
            
            return "/uploads/" + newFileName;

        } catch (IOException e) {
            throw new ConflictException("Error al generar la imagen de perfil por defecto: " + e.getMessage());
        }
    }

    /**
     * Guarda el archivo de imagen de perfil en una carpeta local, usando el nombre de usuario.
     * Carga la imagen en el directorio de subidas y genera un nombre único basado en el usuario y el rol.
     * @param file El archivo de imagen.
     * @param user El objeto User con el nombre de usuario y el rol.
     * @return La ruta relativa del archivo guardado.
     */
    private String saveProfileImage(MultipartFile file, User user) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String newFileName = user.getUsername() + fileExtension;
            
            Path filePath = uploadPath.resolve(newFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + newFileName;
        } catch (IOException e) {
            throw new ConflictException("Error al guardar la nueva imagen de perfil: " + e.getMessage());
        }
    }


    /**
     * Elimina un archivo de imagen de perfil del sistema de archivos si existe.
     * * @param imageUrl La URL o ruta relativa de la imagen a eliminar.
    */
    private void deleteProfileImage(String imageUrl) {
        if (imageUrl != null && !isDefaultProfileImage(imageUrl)) {
            String fileName = imageUrl.replace("/uploads/", "");
            Path filePath = Paths.get(uploadDir, fileName);
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Error al eliminar la imagen de perfil: " + filePath.toString() + ". Mensaje: " + e.getMessage());
            }
        }
    }

    /**
     * Verifica si la imagen actual es la imagen por defecto
     * @param currentImage Ruta de la imagen actual
     * @return true si es la imagen por defecto
    */
    private boolean isDefaultProfileImage(String currentImage) {
        if (currentImage == null) return false;
        return currentImage.equals(getDefaultProfileImagePath()) || 
               currentImage.endsWith("/" + defaultProfileImage);
    }

    /**
     * Valida si el tipo de archivo es una imagen válida
     */
    private boolean isValidImageType(String contentType) {
        return contentType != null && 
            (contentType.equals("image/jpeg") || 
                contentType.equals("image/png") || 
                contentType.equals("image/jpg"));
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

    

   
}
