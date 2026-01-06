package org.example.notebooklm.service;

import org.example.notebooklm.log.RetrievalLogger;
import org.example.notebooklm.model.PdfChunk;
import org.example.notebooklm.repository.PdfChunkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RetrievalService {

    private static final Logger logger = LoggerFactory.getLogger(RetrievalService.class);
    private static final int DEFAULT_TOP_K = 5;

    private final PdfChunkRepository chunkRepository;
    private final SimilarityFilter similarityFilter;
    private final RetrievalLogger retrievalLogger;

    public RetrievalService(PdfChunkRepository chunkRepository,
                            SimilarityFilter similarityFilter,
                            RetrievalLogger retrievalLogger) {
        this.chunkRepository = chunkRepository;
        this.similarityFilter = similarityFilter;
        this.retrievalLogger = retrievalLogger;
    }

    /**
     * Finds similar chunks using a float[] embedding.
     */
    public List<PdfChunk> findSimilarChunks(Long pdfId, float[] queryEmbedding, int topK) {

        if (queryEmbedding == null || queryEmbedding.length == 0) {
            logger.warn("Query embedding is null or empty");
            return List.of();
        }

        // המרה לפורמט ש-pgvector מבין: [0.1, 0.2, ...]
        String queryVector = formatVectorForPostgres(queryEmbedding);
        logger.debug("Searching for top {} similar chunks in document {}", topK, pdfId);

        // 1️⃣ שליפה מה‑DB בלבד
        List<PdfChunk> rawResults = chunkRepository.findSimilarChunks(pdfId, queryVector, topK);
        retrievalLogger.logRawResults(rawResults);

        if (rawResults.isEmpty()) {
            return List.of();
        }

        // 2️⃣ אין distances בשלב זה — מעבירים null לסינון הדינמי
        List<PdfChunk> filtered = similarityFilter.filterByDynamicThreshold(rawResults, null);

        // 3️⃣ לוגים אחרי סינון
        retrievalLogger.logFilteredResults(filtered);

        // 4️⃣ לוגים של מה שנמחק
        List<PdfChunk> removed = rawResults.stream()
                .filter(c -> !filtered.contains(c))
                .toList();

        retrievalLogger.logRemovedChunks(removed);

        return filtered;
    }

    /**
     * Overloaded method with default Top K.
     */
    public List<PdfChunk> findSimilarChunks(Long pdfId, float[] queryEmbedding) {
        return findSimilarChunks(pdfId, queryEmbedding, DEFAULT_TOP_K);
    }

    /**
     * Efficiently formats a float array as a pgvector string.
     */
    private String formatVectorForPostgres(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            sb.append(String.format("%.10f", embedding[i]));
            if (i < embedding.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}