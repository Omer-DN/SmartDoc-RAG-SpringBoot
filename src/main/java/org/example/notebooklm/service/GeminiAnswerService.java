package org.example.notebooklm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiAnswerService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiAnswerService.class);

    private final SecretService secretService;
    private final RestTemplate restTemplate = new RestTemplate();

    public GeminiAnswerService(SecretService secretService) {
        this.secretService = secretService;
    }

    /**
     * Answers a question using the PDF context and question embedding.
     */
    public String answer(Long pdfId, String question, double[] queryEmbedding) {

        // ⭐ לוג בסיסי
        logger.info("GeminiAnswerService: answering question for PDF {}: {}", pdfId, question);

        // ⭐ לוג של embedding
        logger.debug("Query embedding length: {}", queryEmbedding.length);

        // prompt בסיסי
        String prompt = """
                Answer the question using the context of the PDF document with ID %d.

                Question:
                %s

                If the answer is not present in the document, say "I don't know".
                """.formatted(pdfId, question);

        String url =
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key="
                        + secretService.getGeminiKey();

        // ⭐ גוף הבקשה — תקין לפי Gemini
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

        Map<?, ?> response = restTemplate.postForObject(url, entity, Map.class);

        // ⭐ לוג של התשובה הגולמית
        logger.debug("Raw Gemini response: {}", response);

        String answer = extractText(response);

        // ⭐ לוג של התשובה הסופית
        logger.info("Gemini answer: {}", answer);

        return answer;
    }

    private String extractText(Map<?, ?> response) {
        try {
            var candidates = (List<?>) response.get("candidates");
            var content = (Map<?, ?>) candidates.get(0);
            var parts = (List<?>) ((Map<?, ?>) content.get("content")).get("parts");
            return (String) ((Map<?, ?>) parts.get(0)).get("text");
        } catch (Exception e) {
            logger.error("Failed to parse Gemini answer", e);
            return "Failed to parse Gemini answer";
        }
    }
}
