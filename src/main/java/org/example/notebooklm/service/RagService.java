package org.example.notebooklm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RagService {

    private static final Logger logger = LoggerFactory.getLogger(RagService.class);

    private final DemoAnswerService demoAnswerService;

    public RagService(@Autowired(required = false) DemoAnswerService demoAnswerService) {
        this.demoAnswerService = demoAnswerService;
    }

    public String askQuestion(Long pdfId, String question) {
        logger.info("RagService | pdfId={}, question={}", pdfId, question);

        if (demoAnswerService != null) {
            return demoAnswerService.getAnswer(pdfId, question);
        }

        return "I have no information to provide an answer to that question.";
    }
}
