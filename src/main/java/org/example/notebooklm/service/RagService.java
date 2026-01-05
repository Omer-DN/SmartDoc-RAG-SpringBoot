package org.example.notebooklm.service;

import org.example.notebooklm.model.PdfChunk;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagService {

    private final GeminiAnswerService geminiAnswerService;
    private final GeminiEmbeddingService geminiEmbeddingService;
    private final RetrievalService retrievalService;

    private static final int DEFAULT_TOP_K = 5;

    public RagService(GeminiAnswerService geminiAnswerService,
                      GeminiEmbeddingService geminiEmbeddingService,
                      RetrievalService retrievalService) {
        this.geminiAnswerService = geminiAnswerService;
        this.geminiEmbeddingService = geminiEmbeddingService;
        this.retrievalService = retrievalService;
    }

    /**
     * Finds the most relevant chunks and asks Gemini to answer the question.
     *
     * @param pdfId   The PDF document ID
     * @param question The question to answer
     * @return The answer from Gemini
     */
    public String answerQuestion(Long pdfId, String question) {
        // 1️⃣ יצירת embedding של השאלה
        double[] queryEmbedding = geminiEmbeddingService.generateEmbedding(question);

        if (queryEmbedding == null || queryEmbedding.length == 0) {
            return "Failed to generate embedding for question";
        }

        // 2️⃣ שליפת החלקים הכי דומים
        List<PdfChunk> topChunks = retrievalService.findSimilarChunks(pdfId, queryEmbedding, DEFAULT_TOP_K);

        if (topChunks.isEmpty()) {
            return "No relevant content found in PDF";
        }

        // 3️⃣ בחר את החלק הכי דומה
        PdfChunk bestChunk = topChunks.get(0);

        // 4️⃣ שלח ל-Gemini: pdfId + השאלה + embedding
        return geminiAnswerService.answer(pdfId, question, queryEmbedding);
    }
}
