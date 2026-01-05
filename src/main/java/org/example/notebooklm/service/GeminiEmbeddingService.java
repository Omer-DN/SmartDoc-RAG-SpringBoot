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
@Primary
public class GeminiEmbeddingService implements EmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiEmbeddingService.class);

    private final SecretService secretService;
    private final RestTemplate restTemplate = new RestTemplate();

    public GeminiEmbeddingService(SecretService secretService) {
        this.secretService = secretService;
    }

    @Override
    public double[] generateEmbedding(String text) {
        try {
            String url =
                    "https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent?key="
                            + secretService.getGeminiKey();

            Map<String, Object> request = Map.of(
                    "content", Map.of(
                            "parts", List.of(
                                    Map.of("text", text)
                            )
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, entity, Map.class);

            logger.info("Gemini response: {}", response.getBody());

            Map<String, Object> body = response.getBody();
            if (body == null) {
                logger.error("Gemini returned null body");
                return null;
            }

            // ⭐ 1. אם יש שדה error — מחזירים null
            if (body.containsKey("error")) {
                logger.error("Gemini error: {}", body.get("error"));
                return null;
            }

            Object embeddingObj = null;

            // ⭐ 2. פורמט ראשון: "embedding": { "values": [...] }
            if (body.containsKey("embedding")) {
                embeddingObj = body.get("embedding");
            }

            // ⭐ 3. פורמט שני: "embeddings": [ { "values": [...] } ]
            else if (body.containsKey("embeddings")) {
                List<Map<String, Object>> embeddings =
                        (List<Map<String, Object>>) body.get("embeddings");

                if (embeddings != null && !embeddings.isEmpty()) {
                    embeddingObj = embeddings.get(0);
                }
            }

            if (embeddingObj == null) {
                logger.error("Gemini response missing embedding field: {}", body);
                return null;
            }

            Map<String, Object> embedding = (Map<String, Object>) embeddingObj;
            List<Double> values = (List<Double>) embedding.get("values");

            if (values == null || values.isEmpty()) {
                logger.error("Gemini returned empty embedding values");
                return null;
            }

            return values.stream().mapToDouble(Double::doubleValue).toArray();

        } catch (Exception e) {
            logger.error("Failed to generate embedding", e);
            return null;
        }
    }
}
