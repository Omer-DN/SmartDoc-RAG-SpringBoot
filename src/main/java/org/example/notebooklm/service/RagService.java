package org.example.notebooklm.service;

import org.example.notebooklm.model.PdfChunk;
import org.example.notebooklm.repository.PdfChunkRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class RagService {

    private final PdfChunkRepository chunkRepository;
    private final GeminiEmbeddingService geminiEmbeddingService;
    private final GeminiAnswerService geminiAnswerService;

    public RagService(PdfChunkRepository chunkRepository,
                      GeminiEmbeddingService geminiEmbeddingService,
                      GeminiAnswerService geminiAnswerService) {
        this.chunkRepository = chunkRepository;
        this.geminiEmbeddingService = geminiEmbeddingService;
        this.geminiAnswerService = geminiAnswerService;
    }

    // ===========================
    // RAG main flow
    // ===========================
    public String answerQuestion(Long pdfId, String question) {

        List<PdfChunk> chunks = chunkRepository.findByPdfDocumentId(pdfId);
        if (chunks.isEmpty()) {
            return "No chunks found for this PDF";
        }

        // 1️⃣ embedding לשאלה
        double[] questionEmbedding = geminiEmbeddingService.generateEmbedding(question);
        if (questionEmbedding == null) {
            return "Failed to generate embedding for question";
        }

        // 2️⃣ מציאת chunk רלוונטי ביותר
        PdfChunk bestChunk = chunks.stream()
                .filter(c -> c.getEmbedding() != null &&
                        c.getEmbedding().length == questionEmbedding.length)
                .max(Comparator.comparingDouble(c ->
                        cosineSimilarity(questionEmbedding, c.getEmbedding())))
                .orElse(null);

        if (bestChunk == null) {
            return "No relevant chunk found";
        }

        // 3️⃣ שליחה ל-Gemini כדי לנסח תשובה אמיתית
        return geminiAnswerService.answer(bestChunk.getText(), question);
    }

    // ===========================
    // Cosine Similarity
    // ===========================
    private double cosineSimilarity(double[] vecA, double[] vecB) {
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vecA.length; i++) {
            dot += vecA[i] * vecB[i];
            normA += vecA[i] * vecA[i];
            normB += vecB[i] * vecB[i];
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-10);
    }
}
