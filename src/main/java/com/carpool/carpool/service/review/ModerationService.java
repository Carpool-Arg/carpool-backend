package com.carpool.carpool.service.review;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ModerationService {

    private final RestTemplate restTemplate;
    private final String credentialsJson;
    private static final String API_URL = "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze";
    
    /**
     * Constructor de ModerationService.
     * @param restTemplate Cliente HTTP para llamadas a la API de Perspective.
     * @param credentialsJson JSON de credenciales de Service Account.
     */
    public ModerationService(
            RestTemplate restTemplate,
            @Value("${google.credentials.json}") String credentialsJson
    ) {
        this.restTemplate = restTemplate;
        this.credentialsJson = credentialsJson;
    }

    /**
     * Obtiene un Access Token válido de OAuth2 usando las credenciales del Service Account.
     * @return Access Token válido
     * @throws IOException si hay un error al leer las credenciales
     */
    private String getAccessToken() throws IOException {
        GoogleCredentials credentials = ServiceAccountCredentials
                .fromStream(new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8)))
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/userinfo.email"));

        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }

    /**
     * Envía el texto a la Perspective API de Google para analizar su nivel de toxicidad.
     * Si el score de toxicidad es mayor a 0.7, consideramos el texto como ofensivo.
     * @param text El texto a analizar
     * @return true si el texto es considerado tóxico, false en caso contrario o si ocurre un error al conectar con la API
     */
    @SuppressWarnings("rawtypes")
    public boolean isToxic(String text) {
        if (text == null || text.isBlank()) return false;

        log.debug("Analizando toxicidad para: '{}'", text);

        Map<String, Object> requestBody = Map.of(
                "comment", Map.of("text", text),
                "languages", List.of("es"),
                "requestedAttributes", Map.of("TOXICITY", Map.of())
        );

        try {
            // Obtención del token de acceso
            String accessToken = getAccessToken();

            // Configuración de la petición HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            // Envío de la petición HTTP
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, entity, Map.class);

            // Verificación de la respuesta
            if (response.getBody() == null || !response.getBody().containsKey("attributeScores")) {
                log.warn("Respuesta inválida de Perspective API");
                return false;
            }

            // Extracción del score de toxicidad
            Map<?, ?> attributeScores = (Map<?, ?>) response.getBody().get("attributeScores");
            Map<?, ?> toxicity = (Map<?, ?>) attributeScores.get("TOXICITY");
            Map<?, ?> summaryScore = (Map<?, ?>) toxicity.get("summaryScore");

            // Verificación de que el score sea mayor a 0.45
            double scoreValue = ((Number) summaryScore.get("value")).doubleValue();
            log.info("Análisis Perspective API completado. Score: {}", scoreValue);

            return scoreValue > 0.45;

        } catch (IOException e) {
            log.error("Error al obtener Access Token de Google: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error en ModerationService (IA): {}", e.getMessage());
            return false;
        }
    }
}