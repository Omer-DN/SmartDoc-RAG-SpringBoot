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
     *
     * @param pdfDocument The document to save
     * @return The saved document with generated ID
     */
    @Transactional
    public PdfDocument saveDocument(PdfDocument pdfDocument) {
        logger.info("Saving PDF document: {}", pdfDocument.getFilename());
        PdfDocument saved = documentRepository.save(pdfDocument);
        logger.debug("PDF document saved with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Saves a chunk and associates it with a document.
     *
     * @param chunk The chunk to save
     * @param document The document to associate with
     * @return The saved chunk
     */
    @Transactional
    public PdfChunk saveChunk(PdfChunk chunk, PdfDocument document) {
        document.addChunk(chunk);
        PdfChunk saved = chunkRepository.save(chunk);
        logger.debug("Saved chunk {} for document {}", saved.getChunkIndex(), document.getId());
        return saved;
    }

    /**
     * Saves multiple chunks in a batch operation.
     *
     * @param chunks The chunks to save
     * @param document The document to associate with
     */
    @Transactional
    public void saveChunks(List<PdfChunk> chunks, PdfDocument document) {
        logger.info("Saving {} chunks for document {}", chunks.size(), document.getId());
        for (PdfChunk chunk : chunks) {
            document.addChunk(chunk);
            chunkRepository.save(chunk);
        }
        logger.debug("Successfully saved all chunks for document {}", document.getId());
    }

    /**
     * Retrieves a document by ID.
     *
     * @param id The document ID
     * @return Optional containing the document if found
     */
    public Optional<PdfDocument> findDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    /**
     * Retrieves all PDF documents.
     *
     * @return List of all documents
     */
    public List<PdfDocument> findAllDocuments() {
        return documentRepository.findAll();
    }

    /**
     * Retrieves chunks for a specific document.
     *
     * @param documentId The document ID
     * @return List of chunks for the document
     */
    public List<PdfChunk> findChunksByDocumentId(Long documentId) {
        return chunkRepository.findByPdfDocumentId(documentId);
    }

    /**
     * Deletes a document and all its associated chunks.
     *
     * @param id The document ID
     * @return true if document was found and deleted, false otherwise
     */
    @Transactional
    public boolean deleteDocument(Long id) {
        Optional<PdfDocument> doc = documentRepository.findById(id);
        if (doc.isEmpty()) {
            logger.warn("Attempted to delete non-existent document with ID: {}", id);
            return false;
        }

        logger.info("Deleting document {} and its chunks", id);
        documentRepository.delete(doc.get());
        logger.debug("Successfully deleted document {}", id);
        return true;
    }

    /**
     * Deletes all documents and chunks.
     */
    @Transactional
    public void deleteAll() {
        logger.warn("Deleting all documents and chunks");
        chunkRepository.deleteAll();
        documentRepository.deleteAll();
        logger.info("All documents and chunks deleted");
    }
}



