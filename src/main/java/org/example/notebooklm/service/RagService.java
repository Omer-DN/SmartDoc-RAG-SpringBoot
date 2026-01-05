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

    public RagService(PdfChunkRepository chunkRepository, GeminiEmbeddingService geminiEmbeddingService) {
        this.chunkRepository = chunkRepository;
        this.geminiEmbeddingService = geminiEmbeddingService;
    }

    public List<PdfChunk> getChunksForQuestion(Long pdfId) {
        return chunkRepository.findByPdfDocumentId(pdfId);
    }

    public String answerQuestion(Long pdfId, String question) {
        List<PdfChunk> chunks = getChunksForQuestion(pdfId);

        if (chunks.isEmpty())
            return "No chunks found for this PDF";

        double[] questionEmbedding = geminiEmbeddingService.generateEmbedding(question);
        if (questionEmbedding == null)
            return "Failed to generate embedding for question";

        // ⭐ מסננים chunks בלי embedding כדי למנוע NPE
        PdfChunk bestChunk = chunks.stream()
                .filter(c -> c.getEmbedding() != null && c.getEmbedding().length == questionEmbedding.length)
                .max(Comparator.comparingDouble(c ->
                        cosineSimilarity(questionEmbedding, c.getEmbedding())))
                .orElse(null);

        return bestChunk != null ? bestChunk.getText() : "No relevant chunk found";
    }

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
