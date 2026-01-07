package org.example.notebooklm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Profile("demo")
public class DemoAnswerService {

    private static final Logger logger = LoggerFactory.getLogger(DemoAnswerService.class);

    private final Map<Long, Map<String, String>> answersByPdf = new HashMap<>();

    public DemoAnswerService() {
        logger.info("DemoAnswerService initialized");

        // PDF 1 – CV
        Map<String, String> cv = new HashMap<>();
        cv.put("what is the person name?",
                "The person's name is Omer Dayan.\n");
        cv.put("what is the person role?",
                "The person is a Backend & AI Developer.");
        cv.put("how would you describe the person?",
                "A skilled backend developer with strong experience in Java, Python, and AI-driven systems.");
        answersByPdf.put(1L, cv);

        // PDF 2 – Harry Potter
        Map<String, String> hp = new HashMap<>();
        hp.put("who is harry potter?",
                "Harry Potter is a young wizard who survived an attack by Lord Voldemort.");
        hp.put("who are harry's parents?",
                "Harry Potter’s parents are Lily and James Potter.");
        hp.put("where does harry study magic?",
                "Harry studies at Hogwarts School of Witchcraft and Wizardry.");
        hp.put("who is the main villain?",
                "The main villain is Lord Voldemort.");
        answersByPdf.put(2L, hp);

        logger.info("Loaded demo answers for {} PDFs", answersByPdf.size());
    }

    public String getAnswer(Long pdfId, String question) {
        logger.info("DemoAnswerService | pdfId={}, question={}", pdfId, question);

        if (pdfId == null || question == null || question.isBlank()) {
            return fallback();
        }

        Map<String, String> pdfAnswers = answersByPdf.get(pdfId);
        if (pdfAnswers == null) {
            return fallback();
        }

        String normalized = question.toLowerCase().trim();
        return pdfAnswers.getOrDefault(normalized, fallback());
    }

    private String fallback() {
        return "I have no information to provide an answer to that question.";
    }
}
