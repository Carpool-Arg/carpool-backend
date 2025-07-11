package com.carpool.carpool.service.auth.google;

import com.carpool.carpool.dto.user.google.GoogleAuthResponse;
import com.carpool.carpool.enums.user.UserStatus;
import com.carpool.carpool.exception.InvalidGoogleTokenException;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.security.utils.JwtUtils;
import com.carpool.carpool.utils.ResponseUtils;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleAuthImplementation implements  IGoogleAuthService{

    private static final String NAME = "name";
    private static final String ROLE_USER = "ROLE_USER";

    private final UserRepository userRepository;

    @Value("${google.client-id}")
    private String googleClientId;

    /**
     * Realiza el login con Google: si el usuario existe y está activo, se autentica, si no existe, se registra parcialmente con estado {@code PENDING_PROFILE}.
     * @param idTokenString Token id proporcionado por Google.
     * @return Objeto {@link Response} que contiene el {@link GoogleAuthResponse}.
     */
    @Override
    public Response<GoogleAuthResponse> authenticate(String idTokenString) {
        GoogleIdToken idToken = verifyIdToken(idTokenString);
        Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String name = (String) payload.get(NAME);
        User user = createUserIfNotExists(email, name);

        String token = JwtUtils.generateToken(user.getEmail(), List.of(new SimpleGrantedAuthority(ROLE_USER)));
        GoogleAuthResponse response = buildResponseGoogle(user, token);

        return ResponseUtils.buildOKResponse(List.of("Operación exitosa") , response);
    }

    /**
     * Metodo encargado de crear un {@link GoogleAuthResponse} tomando como datos el token generado y los datos del usuario obtenidos del idToken.
     * @param user Objeto {@link User}
     * @param token Token generado por {@link JwtUtils}
     * @return Objeto del tipo {@link GoogleAuthResponse}
     */
    private GoogleAuthResponse buildResponseGoogle(User user, String token) {
        return GoogleAuthResponse.builder()
                .accessToken(token)
                .refreshToken(null)
                .email(user.getEmail())
                .name(user.getName())
                .status(user.getStatus())
                .needsAction(user.getStatus() != UserStatus.ACTIVE)
                .build();
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
        return userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setStatus(UserStatus.PENDING_PROFILE);
            return userRepository.save(newUser);
        });
    }
}
