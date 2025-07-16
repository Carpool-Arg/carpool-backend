package com.carpool.carpool.service.auth.google;

import com.carpool.carpool.dto.google.GoogleAuthResponse;
import com.carpool.carpool.enums.user.UserStatus;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.InvalidGoogleTokenException;
import com.carpool.carpool.model.role.Role;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.role.RoleRepository;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.security.filter.JwtAuthenticationFilter;
import com.carpool.carpool.security.model.CustomUserDetails;
import com.carpool.carpool.security.utils.JwtUtils;
import com.carpool.carpool.security.utils.UserUtils;
import com.carpool.carpool.service.user.UserImplementation;
import com.carpool.carpool.utils.ResponseUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

import static com.carpool.carpool.service.user.UserImplementation.ROLE_USER;

@Service
@RequiredArgsConstructor
public class AuthImplementationGoogle implements IAuthGoogleService {

    private static final String NAME = "name";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Value("${google.client-id}")
    private String googleClientId;

    /**
     * Realiza el login con Google: si el usuario existe y está activo, se autentica, si no existe, se registra parcialmente con estado {@code PENDING_PROFILE}.
     * @param idTokenString Token id proporcionado por Google.
     * @return Objeto {@link Response} que contiene el {@link GoogleAuthResponse} en caso de que el usuario se encuentre con estado {@code ACTIVE}.
     */
    @Override
    public Response<GoogleAuthResponse> authenticate(String idTokenString){
        GoogleIdToken idToken = verifyIdToken(idTokenString);
        Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String name = (String) payload.get(NAME);
        User user = createUserIfNotExists(email, name);

        String token = "";
        String refreshToken = "";
        if(user.getStatus().equals(UserStatus.ACTIVE) || user.getStatus().equals(UserStatus.PENDING_PROFILE)){
            CustomUserDetails userDetail = new CustomUserDetails(user);
            Claims claims = getAuthorities(userDetail);
            token = JwtUtils.generateAccessToken(user.getUsername(), claims);
            refreshToken = JwtUtils.generateRefreshToken(email, claims);
        }

        GoogleAuthResponse response = buildResponseGoogle(user, token, refreshToken);
        //TODO: enviar mensaje en caso de que tenga estado pendiente de verificacion (donde puede solicitar reenvio de email) o suspendido (se ponga en contacto con el soporte)
        return ResponseUtils.buildOKResponse(List.of("Operación exitosa") , response);
    }

    /**
     * Metodo encargado de crear un {@link GoogleAuthResponse} tomando como datos el token generado y los datos del usuario obtenidos del idToken.
     * @param user Objeto {@link User}
     * @param token Token generado por {@link JwtUtils}
     * @return Objeto del tipo {@link GoogleAuthResponse}
     */
    private GoogleAuthResponse buildResponseGoogle(User user, String token, String refreshToken) {
        return GoogleAuthResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .name(user.getName())
                .status(user.getStatus())
                .needsAction(user.getStatus() != UserStatus.ACTIVE)
                .build();
    }

    /**
     * Metodo que obtiene los roles que tiene asociado un usuario.
     * @param userDetail Usuario del tipo {@link CustomUserDetails}
     * @return Objeto {@link Claims}
     */
    private Claims getAuthorities(CustomUserDetails userDetail){
        try {
            return Jwts.claims()
                    .add(JwtAuthenticationFilter.AUTHORITIES, new ObjectMapper().writeValueAsString(userDetail.getAuthorities()))
                    .add(JwtAuthenticationFilter.USERNAME, userDetail.getUsername())
                    .build();
        } catch (Exception e) {
            throw new InvalidGoogleTokenException("Usted no posee los roles necesarios", e);
        }
    }

    /**
     * Metodo encargado de validar si el token que se recibió es válido
     * @param idTokenString Token recibido
     * @return idToken del tipo {@link GoogleIdToken}
     */
    private GoogleIdToken verifyIdToken(String idTokenString){
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if(idToken == null) throw new IllegalArgumentException("El Token ID de Google es inválido");

            return idToken;

        } catch (GeneralSecurityException | IOException e) {
            throw new InvalidGoogleTokenException("Error al verificar el token con Google", e);
        }
    }

    /**
     * Metodo encargado de crear un usuario en caso de que el mismo no se encuentre registrado en la base de datos. En caso de que se encuentre retorna el {@link User}.
     * @param email Email del usuario
     * @param name Nombre del usuario
     * @return Objeto User del tipo {@link User}
     */
    private User createUserIfNotExists(String email, String name){
        return userRepository.findByEmailAndDeletedAtIsNull(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name);

            String username = generateRandomUsername();
            List<Role> roles = getRoles(ROLE_USER);

            newUser.setUsername(username);
            newUser.setRoles(roles);
            newUser.setStatus(UserStatus.PENDING_PROFILE);
            return userRepository.save(newUser);
        });
    }

    /**
     * Metodo encargado de generar un nombre de usuario unico
     * @return Username del tipo {@link String}
     */
    private String generateRandomUsername(){
        String username;
        do {
            username = UserUtils.generateRandomUsername();
        } while (userRepository.findByUsername(username).isPresent());

        return username;
    }

    /**
     * Metodo encargado de obtener roles
     * @param role Rol que se desea obtener
     * @return Lista de roles del tipo {@link List} que contiene {@link Role}
     */
    private List<Role> getRoles(String role){
        List<Role> roles = new ArrayList<>();
        Role optionalRoleUser = roleRepository.findByName(role)
                .orElseThrow(() -> new ConflictException("Rol '" + ROLE_USER + "' no encontrado."));
        roles.add(optionalRoleUser);

        return roles;
    }

}
