package org.example.notebooklm.service;

import org.example.notebooklm.model.PdfChunk;
import org.example.notebooklm.model.PdfDocument;
import org.example.notebooklm.repository.PdfChunkRepository;
import org.example.notebooklm.repository.PdfDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class PdfService {

    private static final Logger logger = LoggerFactory.getLogger(PdfService.class);

    private final PdfDocumentRepository documentRepository;
    private final PdfChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;
    private final ChunkingService chunkingService;
    private final GeminiAnswerService llmService;
    private final TopKSelector topKSelector;

    public PdfService(PdfDocumentRepository documentRepository,
                      PdfChunkRepository chunkRepository,
                      EmbeddingService embeddingService,
                      ChunkingService chunkingService,
                      GeminiAnswerService llmService,
                      TopKSelector topKSelector) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.embeddingService = embeddingService;
        this.chunkingService = chunkingService;
        this.llmService = llmService;
        this.topKSelector = topKSelector;
    }

    public PdfDocument saveDocument(PdfDocument document) {
        return documentRepository.save(document);
    }

    public List<PdfDocument> getAllPdfs() {
        return documentRepository.findAll();
    }

    @Transactional
    public boolean deletePdf(Long pdfId) {
        return documentRepository.findById(pdfId).map(doc -> {
            logger.info("Deleting PDF {} and its chunks", pdfId);
            documentRepository.delete(doc);
            return true;
        }).orElse(false);
    }

    @Transactional
    public void resetAll() {
        logger.warn("Resetting ALL documents and chunks");
        chunkRepository.deleteAll();
        documentRepository.deleteAll();
    }

    @Transactional
    public void processPdf(PdfDocument document, String fullText) {
        if (document == null || document.getId() == null) {
            throw new IllegalArgumentException("PdfDocument must be persisted before processing");
        }

        logger.info("Processing PDF id {} using Gemini Embeddings...", document.getId());

        List<String> chunks = chunkingService.chunk(fullText);
        logger.info("PDF id {} split into {} chunks", document.getId(), chunks.size());

        int index = 0;
        for (String chunkText : chunks) {
            float[] embedding = embeddingService.generateEmbedding(chunkText);

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

    /**
     * פונקציית ה-RAG המרכזית המותאמת ל-Gemini
     */
    public String askQuestion(Long pdfId, String question) {
        logger.info("RAG Request for Gemini - PDF ID: {}, Question: {}", pdfId, question);

        // 1. יצירת וקטור לשאלה (Retrieval Phase) - משתמש ב-GeminiEmbeddingService
        float[] queryEmbedding = embeddingService.generateEmbedding(question);
        if (queryEmbedding == null || queryEmbedding.length == 0) {
            throw new RuntimeException("Could not generate embedding for question");
        }

        // 2. המרה לפורמט וקטורי תקני עבור PostgreSQL/pgvector: [val1,val2,...]
        // התיקון: שימוש ב-Locale.US ובנייה ידנית למניעת שגיאות Syntax ב-SQL
        String vectorString = "[" +
                IntStream.range(0, queryEmbedding.length)
                        .mapToObj(i -> String.format(Locale.US, "%.8f", queryEmbedding[i]))
                        .collect(Collectors.joining(",")) +
                "]";

        // 3. חיפוש הצ'אנקים הכי רלוונטיים ב-DB
        int topK = topKSelector.selectTopK(question);
        List<PdfChunk> similarChunks = chunkRepository.findSimilarChunks(pdfId, vectorString, topK);

        if (similarChunks == null || similarChunks.isEmpty()) {
            logger.warn("No similar chunks found for PDF ID {}", pdfId);
            return "No relevant information found in the document.";
        }

        // 4. איחוד הטקסטים שנמצאו ל-Context אחד (Augmentation Phase)
        String context = similarChunks.stream()
                .map(PdfChunk::getText)
                .collect(Collectors.joining("\n---\n"));

        // 5. שליחה ל-Gemini לקבלת תשובה סופית (Generation Phase)
        return llmService.answer(question, context);
    }
}