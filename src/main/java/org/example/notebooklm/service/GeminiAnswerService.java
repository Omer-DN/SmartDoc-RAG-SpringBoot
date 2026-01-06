package org.example.notebooklm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiAnswerService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiAnswerService.class);

    private final SecretService secretService;
    private final RestTemplate restTemplate;

    public GeminiAnswerService(SecretService secretService) {
        this.secretService = secretService;
        this.restTemplate = new RestTemplate();
    }

    public String answer(String question, String context) {
        String apiKey = secretService.getGeminiKey();
        // שימוש בגרסת המודל המעודכנת והיציבה יותר
        String url = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash-latest:generateContent?key=" + apiKey;

        String prompt = "Context: " + context + "\nQuestion: " + question;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {
                    }
            );
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                logger.error("Gemini HTTP error: status={} body={}", response.getStatusCode(), response.getBody());
                return "Gemini error: " + response.getStatusCode();
            }

            return extractText(response.getBody());
        } catch (Exception e) {
            logger.error("Error calling Gemini", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Extract the generated text from Gemini's response map.
     */
    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> body) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return "No candidates returned from Gemini.";
            }

            Map<String, Object> firstCandidate = candidates.get(0);
            Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
            if (content == null) {
                return "No content in Gemini response.";
            }

            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty()) {
                return "No parts in Gemini response.";
            }

            Object textObj = parts.get(0).get("text");
            return textObj != null ? textObj.toString() : "Empty Gemini response.";
        } catch (Exception e) {
            logger.error("Failed to parse Gemini response", e);
            return "Failed to parse Gemini response: " + e.getMessage();
        }
    }
}