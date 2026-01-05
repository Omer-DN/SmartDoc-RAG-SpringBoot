package org.example.notebooklm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * OpenAI embedding service implementation.
 */
@Service
public class OpenAiEmbeddingService implements EmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiEmbeddingService.class);
    private static final String API_URL = "https://api.openai.com/v1/embeddings";
    private static final String MODEL = "text-embedding-3-small";

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public double[] generateEmbedding(String text) {
        if (text == null || text.isBlank()) {
            logger.warn("Attempted to generate embedding for empty text");
            return null;
        }

        try {
            RestTemplate rest = new RestTemplate();

            // Build JSON body using Jackson
            String body = """
                {
                  "model": "%s",
                  "input": "%s"
                }
                """.formatted(MODEL, text.replace("\"", "'"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String apiKey = System.getenv("OPENAI_API_KEY");
            if (apiKey == null || apiKey.isBlank()) {
                logger.error("OPENAI_API_KEY environment variable is not set");
                return null;
            }
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            logger.debug("Requesting embedding from OpenAI API");
            ResponseEntity<String> response =
                    rest.postForEntity(API_URL, entity, String.class);

            JsonNode json = mapper.readTree(response.getBody());
            logger.debug("Received response from OpenAI embedding API");

            JsonNode arr = json.get("data").get(0).get("embedding");

            double[] vector = new double[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                vector[i] = arr.get(i).asDouble();
            }

            logger.debug("Successfully generated embedding with dimension: {}", vector.length);
            return vector;

        } catch (Exception e) {
            logger.error("Failed to generate embedding from OpenAI API", e);
            return null;
        }
    }
}



