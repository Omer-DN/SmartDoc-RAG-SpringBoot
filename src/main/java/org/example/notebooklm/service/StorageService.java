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
import java.util.Optional;

/**
 * Service responsible for database storage operations.
 * Handles persistence of PDF documents and chunks with transaction management.
 */
@Service
public class StorageService {

    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);

    private final PdfDocumentRepository documentRepository;
    private final PdfChunkRepository chunkRepository;

    public StorageService(PdfDocumentRepository documentRepository,
                          PdfChunkRepository chunkRepository) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
    }

    /**
     * Saves a PDF document to the database.
     */
    @Transactional
    public PdfDocument saveDocument(PdfDocument pdfDocument) {
        logger.info("Saving PDF document metadata: {}", pdfDocument.getFileName());
        PdfDocument saved = documentRepository.save(pdfDocument);
        logger.debug("PDF document saved with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Saves a chunk and associates it with a document.
     */
    @Transactional
    public PdfChunk saveChunk(PdfChunk chunk, PdfDocument document) {
        // קישור ה-Chunk למסמך האב
        document.addChunk(chunk);
        PdfChunk saved = chunkRepository.save(chunk);
        logger.debug("Saved chunk {} for document {}", saved.getChunkIndex(), document.getId());
        return saved;
    }

    /**
     * Saves multiple chunks in a batch operation.
     * שימוש ב-saveAll במקום לולאת save אינדיבידואלית לביצועים משופרים.
     */
    @Transactional
    public void saveChunks(List<PdfChunk> chunks, PdfDocument document) {
        logger.info("Saving batch of {} chunks for document ID: {}", chunks.size(), document.getId());

        // עדכון הקישור הדו-כיווני עבור כל צ'אנק
        for (PdfChunk chunk : chunks) {
            document.addChunk(chunk);
        }

        // ביצוע שמירה מאוחדת (Batch Insert)
        chunkRepository.saveAll(chunks);
        logger.debug("Successfully saved all chunks for document {}", document.getId());
    }

    @Transactional(readOnly = true)
    public Optional<PdfDocument> findDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<PdfDocument> findAllDocuments() {
        return documentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<PdfChunk> findChunksByDocumentId(Long documentId) {
        // וודא שב-Repository קיימת המתודה findByPdfDocumentId
        return chunkRepository.findByPdfDocumentId(documentId);
    }

    @Transactional
    public boolean deleteDocument(Long id) {
        Optional<PdfDocument> doc = documentRepository.findById(id);
        if (doc.isEmpty()) {
            logger.warn("Attempted to delete non-existent document with ID: {}", id);
            return false;
        }

        logger.info("Deleting document {} and all its associated chunks (Cascade)", id);
        // בזכות CascadeType.ALL במודל, המחיקה של ה-Document תמחק אוטומטית את הצ'אנקים
        documentRepository.delete(doc.get());
        logger.debug("Successfully deleted document {}", id);
        return true;
    }

    @Transactional
    public void deleteAll() {
        logger.warn("Request to delete all documents and chunks in the database");
        chunkRepository.deleteAll();
        documentRepository.deleteAll();
        logger.info("Database cleared: All documents and chunks deleted");
    }
}