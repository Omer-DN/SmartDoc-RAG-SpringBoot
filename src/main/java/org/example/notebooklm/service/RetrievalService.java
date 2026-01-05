package org.example.notebooklm.service;

import org.example.notebooklm.log.RetrievalLogger;
import org.example.notebooklm.model.PdfChunk;
import org.example.notebooklm.repository.PdfChunkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<PdfChunk> findSimilarChunks(Long pdfId, double[] queryEmbedding, int topK) {

        if (queryEmbedding == null || queryEmbedding.length == 0) {
            logger.warn("Query embedding is null or empty");
            return List.of();
        }

        String queryVector = formatVectorForPostgres(queryEmbedding);
        logger.debug("Searching for top {} similar chunks in document {}", topK, pdfId);

        // 1️⃣ שליפה מה‑DB בלבד
        List<PdfChunk> rawResults = chunkRepository.findSimilarChunks(pdfId, queryVector, topK);
        retrievalLogger.logRawResults(rawResults);

        if (rawResults.isEmpty()) {
            return List.of();
        }

        // 2️⃣ חילוץ distances
        List<Double> distances = rawResults.stream()
                .map(PdfChunk::getDistance)
                .toList();

        // 3️⃣ סינון לפי threshold דינמי
        List<PdfChunk> filtered = similarityFilter.filterByDynamicThreshold(rawResults, distances);

        // 4️⃣ לוגים אחרי סינון
        retrievalLogger.logFilteredResults(filtered);

        // 5️⃣ לוגים של מה שנמחק
        List<PdfChunk> removed = rawResults.stream()
                .filter(c -> !filtered.contains(c))
                .toList();

        retrievalLogger.logRemovedChunks(removed);

        return filtered;
    }

    public List<PdfChunk> findSimilarChunks(Long pdfId, double[] queryEmbedding) {
        return findSimilarChunks(pdfId, queryEmbedding, DEFAULT_TOP_K);
    }

    private String formatVectorForPostgres(double[] embedding) {
        return "[" + Arrays.stream(embedding)
                .mapToObj(d -> String.format("%.10f", d))
                .collect(Collectors.joining(",")) + "]";
    }
}
