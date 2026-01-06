package org.example.notebooklm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Primary // מבטיח שזה השירות שיוזרק כברירת מחדל
public class GeminiEmbeddingService extends EmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiEmbeddingService.class);

    private final SecretService secretService;
    private final RestTemplate restTemplate = new RestTemplate();

    public GeminiEmbeddingService(SecretService secretService) {
        this.secretService = secretService;
    }

    @Override
    public float[] generateEmbedding(String text) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent?key="
                    + secretService.getGeminiKey();

            Map<String, Object> request = Map.of(
                    "content", Map.of(
                            "parts", List.of(Map.of("text", text))
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            Map<String, Object> body = response.getBody();
            if (body == null || body.containsKey("error")) {
                logger.error("Gemini error: {}", body != null ? body.get("error") : "null body");
                return null;
            }

            Map<String, Object> embeddingMap = (Map<String, Object>) body.get("embedding");
            List<?> values = (List<?>) embeddingMap.get("values");

            if (values == null) return null;

            // המרה ידנית ל-float[] כדי להתאים ל-Entity ולמנוע שגיאות Casting
            float[] floatVector = new float[values.size()];
            for (int i = 0; i < values.size(); i++) {
                floatVector[i] = ((Number) values.get(i)).floatValue();
            }

            return floatVector;

        } catch (Exception e) {
            logger.error("Failed to generate embedding", e);
            return null;
        }
    }
}