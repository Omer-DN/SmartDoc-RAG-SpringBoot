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
    private final GeminiAnswerService llmService;

    public PdfService(PdfDocumentRepository documentRepository,
                      PdfChunkRepository chunkRepository,
                      EmbeddingService embeddingService,
                      ChunkingService chunkingService,
                      GeminiAnswerService llmService) {

        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.embeddingService = embeddingService;
        this.chunkingService = chunkingService;
        this.llmService = llmService;
    }

    public PdfDocument saveDocument(PdfDocument document) {
        logger.info("Saving PDF document: {}", document.getId());
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
            logger.warn("Attempted to delete non-existing PDF: {}", pdfId);
            return false;
        }

        logger.info("Deleting PDF {} and its chunks", pdfId);
        chunkRepository.deleteByPdfDocumentId(pdfId);
        documentRepository.deleteById(pdfId);

        return true;
    }

    public void resetAll() {
        logger.warn("Resetting ALL documents and chunks");
        chunkRepository.deleteAll();
        documentRepository.deleteAll();
    }

    public void processPdf(PdfDocument document, String fullText) {
        if (document == null || document.getId() == null) {
            throw new IllegalArgumentException("PdfDocument must be persisted before processing");
        }

        logger.info("Processing PDF id {}...", document.getId());

        List<String> chunks = chunkingService.chunk(fullText);
        logger.info("PDF id {} split into {} chunks", document.getId(), chunks.size());

        int index = 0;

        for (String chunkText : chunks) {
            double[] embedding = embeddingService.generateEmbedding(chunkText);

            if (embedding == null || embedding.length == 0) {
                logger.error("Embedding generation failed for chunk {}", index);
                continue;
            }

            PdfChunk chunk = new PdfChunk();
            chunk.setText(chunkText);
            chunk.setChunkIndex(index++);
            chunk.setPdfDocument(document);
            chunk.setEmbedding(embedding);

            chunkRepository.save(chunk);
        }

        logger.info("Finished processing PDF {}", document.getId());
    }

    // ⭐⭐⭐ פונקציית askQuestion — הגרסה הנכונה והיחידה ⭐⭐⭐
    public String askQuestion(Long pdfId, String question) {

        logger.info("Received question for PDF {}: {}", pdfId, question);

        double[] queryEmbedding = embeddingService.generateEmbedding(question);

        logger.debug("Query embedding length: {}", queryEmbedding.length);

        if (queryEmbedding == null || queryEmbedding.length == 0) {
            logger.error("Failed to generate embedding for question");
            throw new RuntimeException("Embedding generation failed");
        }

        String vectorString = vectorToString(queryEmbedding);

        logger.debug("Query vector: {}", vectorString);

        List<PdfChunk> similarChunks =
                chunkRepository.findSimilarChunks(pdfId, vectorString, 5);

        if (similarChunks == null || similarChunks.isEmpty()) {
            logger.warn("No similar chunks found for PDF {} and question '{}'", pdfId, question);
            return "I couldn't find relevant information in this PDF.";
        }

        logger.info("Found {} relevant chunks for question", similarChunks.size());

        String answer = llmService.answer(pdfId, question, queryEmbedding);

        logger.info("LLM answer: {}", answer);

        return answer;
    }

    private String vectorToString(double[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
