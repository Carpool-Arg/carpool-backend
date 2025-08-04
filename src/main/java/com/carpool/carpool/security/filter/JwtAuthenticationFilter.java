package com.carpool.carpool.security.filter;

import static com.carpool.carpool.security.config.TokenJwtConfig.CONTENT_TYPE;
import static com.carpool.carpool.security.config.TokenJwtConfig.HEADER_AUTHORIZATION;
import static com.carpool.carpool.security.config.TokenJwtConfig.PREFIX_TOKEN;
import static com.carpool.carpool.utils.EmailMessageUtils.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.carpool.carpool.enums.token.TokenTypeEnum;
import com.carpool.carpool.enums.user.UserStateEnum;
import com.carpool.carpool.model.user.UserToken;
import com.carpool.carpool.repository.user.token.UserTokenRepository;
import com.carpool.carpool.security.utils.JwtUtils;
import com.carpool.carpool.service.email.IEmailService;
import com.carpool.carpool.utils.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.carpool.carpool.dto.security.token.TokenResponseDTO;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.dto.security.login.LoginRequestDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.security.model.CustomUserDetails;
import com.carpool.carpool.service.user.account.IUserAccountService;
import com.carpool.carpool.utils.ResponseUtils;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro personalizado que intercepta peticiones realizadas al endpoint {@link /login} y se encarga de autenticar al usuario con sus credenciales (username y password).
 *
 * Si la autenticación es exitosa, se genera un token JWT y lo devuelve en la response.
 * Si la autenticación no es exitosa, se retorna un mensaje de error personalizado.
 */
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter{

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public final static String AUTHORITIES = "authorities";
    public final static String USERNAME = "username";

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final IUserAccountService userAccountService;
    private final IEmailService emailImplementation;
    private final UserTokenRepository userTokenRepository;

    private String currentUsername;
    private String supportEmail;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, UserRepository userRepository,
                                   IUserAccountService userAccountService, IEmailService emailImplementation,
                                   String supportEmail, UserTokenRepository userTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.userAccountService = userAccountService;
        this.emailImplementation = emailImplementation;
        this.userTokenRepository = userTokenRepository;
        this.supportEmail = supportEmail;
    }

    /**
     * Método que se ejecuta cuando el cliente realiza una petición de login.
     *
     * Obtiene el username y password de la petición y genera un token de autenticación con los mismos, que luego es validado por el
     * {@link AuthenticationManager}.
     *
     * @param request petición HTTP.
     * @param response respuesta HTTP.
     * @return Respuesta de la autenticación.
     * @throws AuthenticationException si ocurre algún error durante la autenticación.
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        LoginRequestDTO userLogin = null;

        try {
            userLogin = new ObjectMapper().readValue(request.getInputStream(), LoginRequestDTO.class);
            this.currentUsername= userLogin.getUsername();
        } catch (StreamReadException e) {
        } catch (IOException e) {
            throw new RuntimeException("Error al leer las credenciales de la petición", e);
        }
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userLogin.getUsername(), userLogin.getPassword());

        return authenticationManager.authenticate(authToken);
    }

    /**
     * Método que se invoca cuando la autenticación es exitosa. Obtiene el usuario autenticado y genera el token JWT.
     *
     * @param request petición HTTP.
     * @param response respuesta HTTP.
     * @param chain cadena de filtros de seguridad.
     * @param authResult resultado de la autenticación.
     *
     * @throws IOException si ocurre algún error durante la autenticación.
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {

        // Obtener el usuario autenticado casteado a CustomUserDetails
        CustomUserDetails authenticatedUser = (CustomUserDetails) authResult.getPrincipal();

        //Extraemos el objeto User del usuario autenticado
        User user = authenticatedUser.getUser();

        //Reseteamos la cantidad de intentos fallidos para ese usuario (a 0)
        userAccountService.resetFailedAttempts(user);


        // Extraer el nombre de usuario del usuario autenticado
        String username = authenticatedUser.getUsername();

        // Obtener los roles/authorities del usuario para incluirlos en el token
        Collection<? extends GrantedAuthority> authorities = authenticatedUser.getAuthorities();

        LOGGER.info("Autenticación exitosa del usuario: {}", username);

        // Construir los claims para el JWT, agregando roles y username
        Claims claims = Jwts.claims()
                .add(AUTHORITIES, new ObjectMapper().writeValueAsString(authorities))
                .add(USERNAME, username)
        .build();

        // Generar el access token con duración de 1 día y los claims
        String accessToken = JwtUtils.generateAccessToken(username, claims);

        // Generar el refresh token con duración de 7 días (604800000 ms) y los mismos claims
        String refreshToken = JwtUtils.generateRefreshToken(username, claims);

        // Agregar el access token en el header Authorization de la respuesta HTTP
        response.addHeader(HEADER_AUTHORIZATION, PREFIX_TOKEN + accessToken);

        // Crear un DTO que contiene ambos tokens para enviarlo en el cuerpo de la respuesta
        TokenResponseDTO tokens = new TokenResponseDTO(accessToken,refreshToken);

        // Construir el ResponseEntity con mensaje de éxito, DTO y código HTTP 200 OK
        ResponseEntity<Response<TokenResponseDTO>> responseBody = new ResponseEntity<>(
            ResponseUtils.buildOKResponse(
                List.of(username + ": Ha iniciado sesión exitosamente."),
                tokens
            ), 
            HttpStatus.OK
        );

        // Escribir el cuerpo de la respuesta en formato JSON y enviarla al cliente
        ResponseUtils.writeResponse(response, responseBody, CONTENT_TYPE);
    }

    /**
     * Método que se invoca cuando la autenticación no es exitosa.
     * Hace el manejo de la cantidad de intentos ed inicio de sesion fallidos de cada usuario, para el
     * control del estado de cada cuenta
     * @param request petición HTTP.
     * @param response respuesta HTTP.
     *
     * @throws IOException si ocurre algún error durante el proceso.
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        //Obtenemos el nombre de usuario de la variable de la clase
        //No se puede obtener de la request porque viene nulo, ya que username y password vienen en el cuerpo de la peticion como JSON
        // no como parametros
        String username = this.currentUsername;
        this.currentUsername = null;
        

        //Buscamos si el usuario existe en la base de datos
        Optional<User> optionalUser = userRepository.findByUsernameAndDeletedAtIsNull(username);
        List<String> messages = new ArrayList<>();
        //Si el usuario existe realizamos toda la logica, sino devolvemos solo un mensaje
        if (optionalUser.isEmpty()){
            messages.add("Nombre de usuario o contraseña incorrecta.");
        }else{
            User user = optionalUser.get();

            //Verificamos que la cuenta no este bloqueada, si esta bloqueda permanetemente enviamos un mensaje indicando la situacion
            //Si la cuenta no esta bloqueada y han pasado mas de 4 horas desde el utimo intento de inicio de sesion del usuario,
            //seteamos la cantidad de intentos fallidos en 0.
            
            switch (user.getStatus()) {
                case LOCKED:
                    messages.add("Su cuenta se encuentra bloqueada permanentemente.");
                    break;
                case ACTIVE:
                    if(userAccountService.resetTimeExpired(user)){
                        user.setFailedAttempts(0);
                    }
                    messages.add(handleActiveAccount(user));
                    break;
                case SUSPENDED:
                    if(userAccountService.resetTimeExpired(user)){
                        user.setFailedAttempts(0);
                    }
                    messages.add(handleSuspendedAccount(user));
                    break;
                case PENDING_PROFILE:
                    messages.add(UserStateEnum.PENDING_PROFILE.toString());
                    messages.add("Deber completar el registro para tener acceso a la aplicación.");
                    break;
                case PENDING_VERIFICATION:
                    messages.add(UserStateEnum.PENDING_VERIFICATION.toString());
                    messages.add("Debe activar su cuenta para tener acceso a la aplicación.");
                    break;
                default:
                    messages.add("Nombre de usuario o contraseña incorrecta");
                    break;
            }
            
        }
        ResponseEntity<Response<Void>> responseBody = new ResponseEntity<>(
            ResponseUtils.buildErrorResponse(messages),
            HttpStatus.UNAUTHORIZED
        );

        ResponseUtils.writeResponse(response, responseBody, CONTENT_TYPE);

    }


    private String handleActiveAccount(User user){
        //Incrementamos la cantidad de intentos fallidos del usuario
        userAccountService.increaseFailedAttempts(user);

        //Esto se hace debido a que el userAccountService acutaliza el objeto en la base de datos, pero
        //esta actualizacion no se ve reflejada en el objeto que se tiene guardado en memoria, por lo que hay que hacerlo
        //manualmente
        user.setFailedAttempts(user.getFailedAttempts() + 1);

        /*
        * Seteamos la fecha actual como ultimo intento de inicio fallido y guardamos el usuario en la base de datos
        */
        user.setLastFailedLoginTime(new Date());
        userRepository.save(user);

        //Segun la cantidad de intentos fallidos que tenga el usuario hacemos las acciones correspondientes
        /*
            * Con 4 intentos avisamos que en el siguiente se va a suspender la cuenta por 15 minutos
            * Con 5 avisamos que la cuenta ha sido suspendida y cambiamos el estado de la cuenta
            * Lo mismo para 9 y 10 pero con 10 bloqueamos la cuenta
            */
        switch (user.getFailedAttempts()) {
            case 4:
                return "Ingreso fallido. Si ingresa mal su contraseña nuevamente su cuenta sera suspendida por 15 minutos.";
            case 5:
                userAccountService.suspendAccount(user);
                return "Ha ingresado incorrectamente su contraseña 5 veces. Su cuenta se encuentra suspendida por los proximos 15 minutos.";
            case 9:
                return "Ingreso fallido. Si ingresa mal su contraseña nuevamente su cuenta sera bloqueada permanentemente!";
            case 10:
                //TODO: ver endpoint al redireccionar cuenta
                String emailContent = supportEmail + "?subject=Cuenta%20bloqueada&body=Hola%2C%20mi%20cuenta%20fue%20bloqueada...";
                String message = String.format(MESSAGE_EMAIL_LOCKED, emailContent);
                saveRequestUnlockAccount(user, message);
                userAccountService.lockAccount(user);
                return "Ha ingresado incorrectamente su contraseña 10 veces. Su cuenta se encuentra bloqueada permanentemente.";
            default:
                return "Nombre de usuario o contraseña incorrecta.";

        }
    } 

    private String handleSuspendedAccount(User user){
        if(userAccountService.lockTimeExpired(user)){
            userAccountService.unSuspendAccount(user);
            return "La cuenta se encuentra desbloqueada. Por favor, intente ingresar nuevamente";
        }else{
            user.setLastFailedLoginTime(new Date());
            userRepository.save(user);
            return "Su cuenta se encuentra suspendida por repetidos intentos de inicio de sesión. Vuelva a intentarlo mas tarde";
        }
    }

    /**
     * Metodo que se encarga de almacenar una solicitud para activar la cuenta del usuario en la base de datos.
     * @param user Objeto del tipo {@link User}
     * @param message Mensaje que se va a mostrar en el email del tipo {@link String}
     */
    private void saveRequestUnlockAccount(User user, String message){
        UserToken userToken = TokenUtils.buildUserToken(user, TokenTypeEnum.ACTIVATION, userTokenRepository);
        userTokenRepository.save(userToken);
        emailImplementation.sendEmail(user.getEmail(), SUBJECT_EMAIL_LOCKED, TITLE_LOCKED.replace("{name}", user.getName()), message, null, "http://localhost:3000/unlocked", UNLOCKED, MESSAGE_FOOTER_LOCKED);
    }
}
