package org.example.notebooklm.service;

import org.example.notebooklm.model.PdfChunk;
import org.example.notebooklm.repository.PdfChunkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class RagService {

    private static final Logger logger = LoggerFactory.getLogger(RagService.class);

    private final PdfChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;
    private final GeminiAnswerService geminiAnswerService;

    public RagService(PdfChunkRepository chunkRepository,
                      EmbeddingService embeddingService,
                      GeminiAnswerService geminiAnswerService) {
        this.chunkRepository = chunkRepository;
        this.embeddingService = embeddingService;
        this.geminiAnswerService = geminiAnswerService;
    }

    public String askQuestion(Long pdfId, String question) {
        logger.info("Starting RAG process for PDF ID: {} with question: {}", pdfId, question);

        // 1. יצירת Embedding לשאלה
        float[] queryEmbedding = embeddingService.generateEmbedding(question);
        if (queryEmbedding == null || queryEmbedding.length == 0) {
            throw new RuntimeException("Failed to generate embedding for question");
        }

        // 2. המרה לפורמט וקטורי עבור PostgreSQL ללא רווחים וללא שגיאות פורמט
        String vectorString = formatVectorForPostgres(queryEmbedding);

        // 3. חיפוש 5 הצ'אנקים הכי דומים בבסיס הנתונים
        List<PdfChunk> similarChunks = chunkRepository.findSimilarChunks(pdfId, vectorString, 5);

        if (similarChunks == null || similarChunks.isEmpty()) {
            return "I couldn't find any relevant information in the uploaded document.";
        }

        // 4. איחוד הטקסטים שנמצאו ל-Context אחד
        String context = similarChunks.stream()
                .map(PdfChunk::getText)
                .collect(Collectors.joining("\n---\n"));

        // 5. שליחה ל-Gemini - תיקון השגיאה כאן!
        // אנחנו שולחים רק (question, context) כפי שנדרש ב-GeminiAnswerService
        return geminiAnswerService.answer(question, context);
    }

    /**
     * Efficiently formats a float array as a pgvector string.
     */
    private String formatVectorForPostgres(float[] embedding) {
        return "[" +
                IntStream.range(0, embedding.length)
                        .mapToObj(i -> String.format(Locale.US, "%.10f", embedding[i]))
                        .collect(Collectors.joining(",")) +
                "]";
    }
}