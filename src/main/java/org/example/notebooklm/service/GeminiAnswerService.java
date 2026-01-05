package org.example.notebooklm.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiAnswerService {

    private final SecretService secretService;
    private final RestTemplate restTemplate = new RestTemplate();

    public GeminiAnswerService(SecretService secretService) {
        this.secretService = secretService;
    }

    public String answer(String context, String question) {

        String prompt = """
        Answer the question ONLY using the information below.

        Context:
        %s

        Question:
        %s

        If the answer is not present, say "I don't know".
        """.formatted(context, question);

        String url =
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key="
                        + secretService.getGeminiKey();

        var body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<?> entity = new HttpEntity<>(body, headers);

        Map<?, ?> response =
                restTemplate.postForObject(url, entity, Map.class);

        return extractText(response);
    }

    private String extractText(Map<?, ?> response) {
        try {
            var candidates = (List<?>) response.get("candidates");
            var content = (Map<?, ?>) candidates.get(0);
            var parts =
                    (List<?>) ((Map<?, ?>) content.get("content")).get("parts");
            return (String) ((Map<?, ?>) parts.get(0)).get("text");
        } catch (Exception e) {
            return "Failed to parse Gemini answer";
        }
    }
}
