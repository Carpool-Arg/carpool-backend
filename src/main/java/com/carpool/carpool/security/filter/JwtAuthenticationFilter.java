package com.carpool.carpool.security.filter;

import static com.carpool.carpool.security.config.TokenJwtConfig.CONTENT_TYPE;
import static com.carpool.carpool.security.config.TokenJwtConfig.HEADER_AUTHORIZATION;
import static com.carpool.carpool.security.config.TokenJwtConfig.PREFIX_TOKEN;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
import com.carpool.carpool.enums.UserStatus;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.dto.security.login.loginRequestDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.security.model.CustomUserDetails;
import com.carpool.carpool.security.utils.JwtUtils;
import com.carpool.carpool.service.account.IUserAccountService;
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

    public final static String AUTHORITIES = "authorities";
    private final static String USERNAME = "username";

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private AuthenticationManager authenticationManager;

    private final JwtUtils jwtUtils;

    private final UserRepository userRepository;

    private final IUserAccountService userAccountService;

    private String currentUsername;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserRepository userRepository,
    IUserAccountService userAccountService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.userAccountService = userAccountService;
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

        loginRequestDTO userLogin = null;

        try {
            userLogin = new ObjectMapper().readValue(request.getInputStream(), loginRequestDTO.class);
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
        String username = user.getUsername();

        // Obtener los roles/authorities del usuario para incluirlos en el token
        Collection<? extends GrantedAuthority> authorities = authenticatedUser.getAuthorities();

        LOGGER.info("AUTENTICACION EXITOSA DEL USUARIO: {}", username);

        // Construir los claims para el JWT, agregando roles y username
        Claims claims = Jwts.claims()
                .add(AUTHORITIES, new ObjectMapper().writeValueAsString(authorities))
                .add(USERNAME, username)
        .build();

        // Generar el access token con duración de 1 día y los claims
        String accessToken = jwtUtils.generateAccessToken(username, claims);

        // Generar el refresh token con duración de 7 días (604800000 ms) y los mismos claims
        String refreshToken = jwtUtils.generateRefreshToken(username, claims);

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
        //Si el usuario existe realizamos toda la logica
        if(optionalUser.isPresent()){
            User user = optionalUser.get();


            //Verificamos que la cuenta no este bloqueada, si esta bloqueda permanetemente enviamos un mensaje indicando la situacion
            //Si la cuenta no esta bloqueada y han pasado mas de 4 horas desde el utimo intento de inicio de sesion del usuario,
            //seteamos la cantidad de intentos fallidos en 0.
            if(user.getAccountStatus() == UserStatus.LOCKED){
                messages.add("Su cuenta se encuentra bloqueada permanentemente.");
            }else if(userAccountService.resetTimeExpired(user)){
                user.setFailedAttempts(0);
            }

            //Si la cuenta esta activa realizamos las comprobaciones para saber cuantos intentos fallidos lleva el usuario
            if(user.getAccountStatus() == UserStatus.ACTIVE){

                //Incrementamos la cantidad de intentos fallidos del usuario
                userAccountService.increaseFailedAttempts(user);

                //Esto se hace debido a que el userAccountService acutaliza el objeto en la base de datos, pero
                //esta actualizacion no se ve reflejada en el objeto que se tiene guardado en memoria, por lo que hay que hacerlo
                //manualmente
                user.setFailedAttempts(user.getFailedAttempts() + 1);

                //Segun la cantidad de intentos fallidos que tenga el usuario hacemos las acciones correspondientes
                /*
                 * Con 4 intentos avisamos que en el siguiente se va a suspender la cuenta por 15 minutos
                 * Con 5 avisamos que la cuenta ha sido suspendida y cambiamos el estado de la cuenta
                 * Lo mismo para 9 y 10 pero con 10 bloqueamos la cuenta
                 */
                switch (user.getFailedAttempts()) {
                    case 4:
                        messages.add("Ingreso fallido. Si ingresa mal su contraseña nuevamente su cuenta sera suspendida por 15 minutos.");
                        break;
                    case 5:
                        userAccountService.suspendAccount(user);
                        messages.add("Ha ingresado incorrectamente su contraseña 5 veces. Su cuenta se encuentra suspendida por los proximos 15 minutos.");
                        break;
                    case 9:
                        messages.add("Ingreso fallido. Si ingresa mal su contraseña nuevamente su cuenta sera bloqueada permanentemente!!");
                        break;
                    case 10:
                        userAccountService.lockAccount(user);
                        messages.add("Ha ingresado incorrectamente su contraseña 10 veces. Su cuenta se encuentra bloqueada permanentemente.");
                        break;
                    default:
                        messages.add("Error en la autenticación");
                        break;
                }
            /*
             * Si la cuenta del usuario esta suspendida verificamos si el tiempo de suspension de 15 ha pasado
             * Si ya paso indicamos al usuario que puede volver a intentar acceder a la cuenta y la desuspendemos
             * Esta desuspension no reinicia la cantidad de intentos, solo la pasa a estado activa
             * Si la cuenta aun esta suspendida enviamos un mensaje
             */
            }else if(user.getAccountStatus() == UserStatus.SUSPENDED){
                if(userAccountService.lockTimeExpired(user)){
                    userAccountService.unSuspendAccount(user);
                    messages.add("La cuenta se encuentra desbloqueada. Por favor, intente ingresar nuevamente");
                }else{
                    messages.add("Su cuenta se encuentra suspendida por repetidos intentos de inicio de sesión. Vuelva a intentarlo mas tarde");
                }
            }

            /*
             * Seteamos la fecha actual como ultimo intento de inicio fallido y guardamos el usuario en la base de datos
             */
            user.setLastFailedLoginTime(new Date());
            userRepository.save(user);
        /*
         * Si el usuario no existe solo indicamos que hubo un error de autenticacion
         */
        }else{
            messages.add("Nombre de usuario o contraseña incorrecta");
        }

        ResponseEntity<Response<Void>> responseBody = new ResponseEntity<>(
            ResponseUtils.buildErrorResponse(messages),
            HttpStatus.UNAUTHORIZED);
        ResponseUtils.writeResponse(response, responseBody, CONTENT_TYPE);
    }
}
