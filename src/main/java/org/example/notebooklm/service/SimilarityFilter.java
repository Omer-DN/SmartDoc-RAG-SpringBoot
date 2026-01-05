package org.example.notebooklm.service;

import org.example.notebooklm.model.PdfChunk;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SimilarityFilter {

    public List<PdfChunk> filterByDynamicThreshold(List<PdfChunk> chunks, List<Double> distances) {

        // אין distances יותר — מחזירים את מה שה‑DB כבר דירג
        return chunks;
    }
}
