package org.example.notebooklm.service;

import org.example.notebooklm.exception.IngestionException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for PDF ingestion: text extraction and chunking.
 * Separated from storage and embedding generation concerns.
 */
@Service
public class IngestionService {

    private static final Logger logger = LoggerFactory.getLogger(IngestionService.class);
    private static final int DEFAULT_CHUNK_SIZE = 1000;

    /**
     * Extracts text content from a PDF file.
     *
     * @param inputStream The PDF file input stream
     * @return Extracted text content
     * @throws IngestionException if PDF extraction fails
     */
    public String extractTextFromPdf(InputStream inputStream) {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            logger.info("Successfully extracted text from PDF ({} characters)", text.length());
            return text;
        } catch (Exception e) {
            logger.error("Failed to extract text from PDF", e);
            throw new IngestionException("Failed to extract text from PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Splits text into chunks of specified maximum size.
     *
     * @param text The text to chunk
     * @param maxChars Maximum characters per chunk
     * @return List of text chunks
     */
    public List<String> splitIntoChunks(String text, int maxChars) {
        if (text == null || text.isEmpty()) {
            logger.warn("Attempted to chunk empty text");
            return new ArrayList<>();
        }

        List<String> chunks = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            if (current.length() + word.length() + 1 > maxChars) {
                chunks.add(current.toString().trim());
                current = new StringBuilder();
            }
            current.append(word).append(" ");
        }

        if (!current.isEmpty()) {
            chunks.add(current.toString().trim());
        }

        logger.debug("Split text into {} chunks (max size: {})", chunks.size(), maxChars);
        return chunks;
    }

    /**
     * Splits text into chunks using default chunk size.
     *
     * @param text The text to chunk
     * @return List of text chunks
     */
    public List<String> splitIntoChunks(String text) {
        return splitIntoChunks(text, DEFAULT_CHUNK_SIZE);
    }

    /**
     * Validates that a file is a valid PDF.
     *
     * @param file The file to validate
     * @throws IngestionException if file is invalid
     */
    public void validatePdfFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IngestionException("PDF file is empty or null");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new IngestionException("File must be a PDF (.pdf extension required)");
        }

        logger.debug("PDF file validation passed: {}", filename);
    }
}

