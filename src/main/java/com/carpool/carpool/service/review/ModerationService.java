package com.carpool.carpool.service.review;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; 
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

@Slf4j 
@Service
@RequiredArgsConstructor 
public class ModerationService {
    
    @Value("${google.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate; 
    private final String API_URL = "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=API_KEY";

    /**
     * Envía el texto a la Perspective API de Google para analizar su nivel de toxicidad.
     * Si el score de toxicidad es mayor a 0.7, consideramos el texto como ofensivo.
     * @param text El texto a analizar
     * @return true si el texto es considerado tóxico, false en caso contrario o si ocurre un error al conectar con la API
     */
    public boolean isToxic(String text) {
        if (text == null || text.isBlank()) return false;

        log.debug("Enviando texto a Perspective API...");

        Map<String, Object> request = Map.of(
            "comment", Map.of("text", text),
            "languages", List.of("es"),
            "requestedAttributes", Map.of("TOXICITY", Map.of())
        );

        try {
            // En lugar de usar .replace(), dejamos que UriComponentsBuilder maneje la key
            // O simplemente concatenamos de forma limpia:
            String finalUrl = "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + apiKey;

            // Usamos postForObject directamente con la URL que ya tiene la clave
            Map<?, ?> response = restTemplate.postForObject(finalUrl, request, Map.class);
            
            if (response == null) return false;
            
            Map<?, ?> attributeScores = (Map<?, ?>) response.get("attributeScores");
            Map<?, ?> toxicity = (Map<?, ?>) attributeScores.get("TOXICITY");
            Map<?, ?> summaryScore = (Map<?, ?>) toxicity.get("summaryScore");
            
            double scoreValue = ((Number) summaryScore.get("value")).doubleValue();
            log.info("Análisis de toxicidad completado. Score: {}", scoreValue);

            return scoreValue > 0.7; 
        } catch (Exception e) {
            log.error("Error en ModerationService: {}", e.getMessage());
            return false; 
        }
    }
}
