package org.example.notebooklm.service;

import org.example.notebooklm.model.PdfChunk;
import org.example.notebooklm.model.PdfDocument;
import org.example.notebooklm.repository.PdfDocumentRepository;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PdfService {

    private static final Logger log = LoggerFactory.getLogger(PdfService.class);

    private final PdfDocumentRepository pdfDocumentRepository;

    public PdfService(PdfDocumentRepository pdfDocumentRepository) {
        this.pdfDocumentRepository = pdfDocumentRepository;
    }

    public boolean deletePdf(Long id) {
        return pdfDocumentRepository.findById(id)
                .map(pdf -> {
                    log.info("Deleting PDF with id {}", id);
                    pdfDocumentRepository.delete(pdf);
                    return true;
                })
                .orElse(false);
    }
    public List<PdfDocument> getAllPdfs() {
        return pdfDocumentRepository.findAll();
    }

    public List<PdfChunk> getChunksForPdf(Long id) {
        return pdfDocumentRepository.findById(id)
                .map(PdfDocument::getChunks)
                .orElse(null);
    }



    public PdfDocument savePdf(byte[] fileBytes, String filename) throws IOException {

        log.info("Starting PDF upload: {}", filename);

        // המרת PDF לטקסט
        PDDocument document = PDDocument.load(fileBytes);
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        document.close();

        log.info("PDF text extracted. Length: {} characters", text.length());

        // יצירת PdfDocument
        PdfDocument pdfDocument = new PdfDocument();
        pdfDocument.setFilename(filename);
        pdfDocument.setContent(text);

        // פיצול טקסט ל־chunks
        List<PdfChunk> chunks = splitTextToChunks(text, 500);
        log.info("Created {} chunks for PDF {}", chunks.size(), filename);

        for (int i = 0; i < chunks.size(); i++) {
            PdfChunk chunk = chunks.get(i);
            chunk.setChunkIndex(i);
            chunk.setPdfDocument(pdfDocument);
        }

        pdfDocument.setChunks(chunks);

        // שמירה ב־DB
        pdfDocumentRepository.save(pdfDocument);

        log.info("PDF {} saved successfully with {} chunks", filename, chunks.size());

        return pdfDocument;
    }

    private List<PdfChunk> splitTextToChunks(String text, int chunkSize) {
        List<PdfChunk> chunks = new ArrayList<>();
        int length = text.length();

        log.info("Splitting text into chunks of {} characters", chunkSize);

        for (int i = 0; i < length; i += chunkSize) {
            int end = Math.min(length, i + chunkSize);
            String chunkText = text.substring(i, end);
            PdfChunk chunk = new PdfChunk();
            chunk.setText(chunkText);
            chunks.add(chunk);
        }

        return chunks;
    }
}
