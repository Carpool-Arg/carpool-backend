package com.carpool.carpool.service.auth.google;

import static com.carpool.carpool.service.user.register.UserRegisterImplementation.ROLE_USER;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.carpool.carpool.dto.google.GoogleAccessTokenRequest;
import com.carpool.carpool.dto.google.GoogleAuthResponse;
import com.carpool.carpool.enums.user.UserStateEnum;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.UnauthorizedException;
import com.carpool.carpool.model.role.Role;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.role.RoleRepository;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.security.filter.JwtAuthenticationFilter;
import com.carpool.carpool.security.model.CustomUserDetails;
import com.carpool.carpool.security.utils.JwtUtils;
import com.carpool.carpool.security.utils.UserUtils;
import com.carpool.carpool.utils.ResponseUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthGoogleImplementation implements IAuthGoogleService {

    private static final String NAME = "name";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.client-id}")
    private String googleClientId;

    @Override
    public Response<GoogleAuthResponse> authenticate(GoogleAccessTokenRequest request){
        Map<String, Object> googleUser =
            getGoogleUserInfo(request.getAccessToken());

        String email = (String) googleUser.get("email");
        String name = (String) googleUser.get(NAME);
        User user = createUserIfNotExists(email, name);

        String token = "";
        String refreshToken = "";

        if(user.getStatus().equals(UserStateEnum.ACTIVE)
            || user.getStatus().equals(UserStateEnum.PENDING_PROFILE)){

            CustomUserDetails userDetail = new CustomUserDetails(user);

            Claims claims = getAuthorities(userDetail);

            token = JwtUtils.generateAccessToken(
                    user.getUsername(),
                    claims
            );

            refreshToken = JwtUtils.generateRefreshToken(
                    email,
                    claims
            );
        }

        GoogleAuthResponse response =
                buildResponseGoogle(user, token, refreshToken);

        return ResponseUtils.buildOKResponse(
                List.of("Operación exitosa"),
                response
        );
    }

    private Map<String, Object> getGoogleUserInfo(String accessToken){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v3/userinfo",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            Map<String, Object> body = response.getBody();

            if(body == null || body.get("email") == null){
                throw new UnauthorizedException("No se pudo obtener el usuario de Google");
            }

            return body;

        } catch (Exception e){
            throw new UnauthorizedException("Token de Google inválido", e);
        }
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
                .needsAction(user.getStatus() != UserStateEnum.ACTIVE)
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
            throw new UnauthorizedException("Usted no posee los roles necesarios", e);
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
            newUser.setStatus(UserStateEnum.PENDING_PROFILE);
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
