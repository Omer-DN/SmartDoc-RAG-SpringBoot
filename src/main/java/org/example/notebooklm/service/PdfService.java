package org.example.notebooklm.service;

import org.example.notebooklm.model.PdfChunk;
import org.example.notebooklm.model.PdfDocument;
import org.example.notebooklm.repository.PdfChunkRepository;
import org.example.notebooklm.repository.PdfDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PdfService {

    private static final Logger logger = LoggerFactory.getLogger(PdfService.class);

    private final PdfDocumentRepository documentRepository;
    private final PdfChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;
    private final ChunkingService chunkingService;

    public PdfService(PdfDocumentRepository documentRepository,
                      PdfChunkRepository chunkRepository,
                      EmbeddingService embeddingService,
                      ChunkingService chunkingService) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.embeddingService = embeddingService;
        this.chunkingService = chunkingService;
    }

    public PdfDocument saveDocument(PdfDocument document) {
        return documentRepository.save(document);
    }

    public List<PdfDocument> getAllPdfs() {
        return documentRepository.findAll();
    }

    public List<PdfChunk> getChunksForPdf(Long pdfId) {
        return chunkRepository.findByPdfDocumentId(pdfId);
    }

    public boolean deletePdf(Long pdfId) {
        if (!documentRepository.existsById(pdfId)) {
            return false;
        }

        chunkRepository.deleteByPdfDocumentId(pdfId);
        documentRepository.deleteById(pdfId);

        return true;
    }

    public void resetAll() {
        chunkRepository.deleteAll();
        documentRepository.deleteAll();
    }

    public void processPdf(PdfDocument document, String fullText) {
        if (document == null || document.getId() == null) {
            throw new IllegalArgumentException("PdfDocument must be persisted before processing");
        }

        List<String> chunks = chunkingService.chunk(fullText);

        int index = 0;

        for (String chunkText : chunks) {
            double[] embedding = embeddingService.generateEmbedding(chunkText);

            PdfChunk chunk = new PdfChunk();
            chunk.setText(chunkText);
            chunk.setChunkIndex(index++);
            chunk.setPdfDocument(document);
            chunk.setEmbedding(embedding);

            chunkRepository.save(chunk);
        }
    }
}
