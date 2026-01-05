package org.example.notebooklm.logs;

import org.example.notebooklm.model.PdfChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RetrievalLogger {

    private static final Logger logger = LoggerFactory.getLogger(RetrievalLogger.class);

    public void logRawResults(List<PdfChunk> chunks) {
        logger.info("Retrieved {} chunks BEFORE filtering", chunks.size());
        chunks.forEach(c ->
                logger.info("Chunk {} | distance={}", c.getChunkIndex(), c.getDistance())
        );
    }

    public void logFilteredResults(List<PdfChunk> chunks) {
        logger.info("Remaining {} chunks AFTER filtering", chunks.size());
        chunks.forEach(c ->
                logger.info("Kept chunk {} | distance={}", c.getChunkIndex(), c.getDistance())
        );
    }

    public void logRemovedChunks(List<PdfChunk> removed) {
        if (removed.isEmpty()) {
            logger.info("No chunks were removed during filtering");
            return;
        }

        removed.forEach(c ->
                logger.info("Removed chunk {} | distance={}", c.getChunkIndex(), c.getDistance())
        );
    }
}
