package org.example.notebooklm.service;

import org.example.notebooklm.model.PdfChunk;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SimilarityFilter {

    public List<PdfChunk> filterByDynamicThreshold(List<PdfChunk> chunks, List<Double> distances) {

        if (distances.isEmpty()) {
            return chunks;
        }

        double mean = distances.stream().mapToDouble(d -> d).average().orElse(0.0);

        double variance = distances.stream()
                .mapToDouble(d -> Math.pow(d - mean, 2))
                .average()
                .orElse(0.0);

        double stdDev = Math.sqrt(variance);

        double threshold = mean + stdDev;

        return chunks.stream()
                .filter(c -> c.getDistance() <= threshold)
                .toList();
    }
}
