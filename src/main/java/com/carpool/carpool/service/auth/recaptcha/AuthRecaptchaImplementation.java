package com.carpool.carpool.service.auth.recaptcha;

import com.carpool.carpool.dto.security.recaptcha.RecaptchaResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthRecaptchaImplementation implements IAuthRecaptchaService {

    @Value("${recaptcha.secretKey}")
    private String secretKey;

    @Value("${recaptcha.verifyUrl}")
    private String verifyUrl;

    private final RestTemplate restTemplate;

    public AuthRecaptchaImplementation(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Valida un token de reCAPTCHA v3 contra la API de verificación de Google.
     * <p>
     * Este método realiza una solicitud HTTP POST al endpoint
     * {@code https://www.google.com/recaptcha/api/siteverify} con los siguientes
     * parámetros:
     * <ul>
     *   <li><strong>secret</strong>: clave secreta del backend (proporcionada por Google)</li>
     *   <li><strong>response</strong>: token generado en el frontend por {@code grecaptcha.execute()}</li>
     * </ul>
     * Google responde con un objeto JSON que contiene el resultado de la validación,
     * incluyendo campos como {@code success}, {@code score}, {@code action}, etc.
     * <p>
     * Este método transforma esa respuesta en una instancia de {@link RecaptchaResponseDTO}.
     *
     * @param recaptchaToken el token generado por el frontend (enviado desde el cliente)
     * @return una instancia de {@link RecaptchaResponseDTO} con los datos de validación devueltos por Google
     */
    @Override
    public RecaptchaResponseDTO validateToken(String recaptchaToken) {
        // Crear headers HTTP: Content-Type = application/x-www-form-urlencoded
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Crear el cuerpo del request (form-data) con los parámetros requeridos
        MultiValueMap<String,String> map = new LinkedMultiValueMap<>();
        map.add("secret", secretKey); // clave secreta del backend
        map.add("response",recaptchaToken); // token que mandó el frontend

        // Construir el request completo: body + headers
        HttpEntity<MultiValueMap<String,String>> entity = new HttpEntity<>(map,headers);

        // Enviar el request POST a Google y esperar la respuesta
        ResponseEntity<RecaptchaResponseDTO> response = restTemplate.exchange(
                verifyUrl, // URL del endpoint de Google
                HttpMethod.POST, // Metodo POST
                entity, // Request completo (headers + body)
                RecaptchaResponseDTO.class // Tipo de respuesta esperada
        );

        // Devolver el cuerpo de la respuesta (objeto con success, score y demas)
        return response.getBody();
    }
}
