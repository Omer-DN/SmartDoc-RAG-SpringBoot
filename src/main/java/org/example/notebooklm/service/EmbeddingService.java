package org.example.notebooklm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmbeddingService {

    private static final String API_URL = "https://api.openai.com/v1/embeddings";

    private static final String MODEL = "text-embedding-3-small";

    private final ObjectMapper mapper = new ObjectMapper();

    public double[] generateEmbedding(String text) {
        System.out.println("API KEY = " + System.getenv("OPENAI_API_KEY"));

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
            headers.set("Authorization", "Bearer " + System.getenv("OPENAI_API_KEY"));

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response =
                    rest.postForEntity(API_URL, entity, String.class);

            JsonNode json = mapper.readTree(response.getBody());
            System.out.println("Embedding API response: " + response.getBody());

            JsonNode arr = json.get("data").get(0).get("embedding");

            double[] vector = new double[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                vector[i] = arr.get(i).asDouble();
            }

            return vector;

        } catch (Exception e) {
            System.out.println("Embedding ERROR: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

    }
}
