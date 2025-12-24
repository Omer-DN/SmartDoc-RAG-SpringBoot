package org.example.notebooklm.service;

import org.example.notebooklm.model.PdfDocument;
import org.example.notebooklm.repository.PdfDocumentRepository;
import org.example.notebooklm.util.TextChunker;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PdfService {

    private final PdfDocumentRepository pdfDocumentRepository;
    private final TextChunker textChunker;

    public PdfService(PdfDocumentRepository pdfDocumentRepository, TextChunker textChunker) {
        this.pdfDocumentRepository = pdfDocumentRepository;
        this.textChunker = textChunker;
    }

    public PdfDocument savePdf(byte[] fileBytes, String filename) throws IOException {
        // המרת PDF לטקסט
        PDDocument document = PDDocument.load(fileBytes);
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        document.close();

        // פיצול טקסט ל־chunks
        var chunks = textChunker.splitText(text, 500); //  500 תווים לכל chunk

        // שמירה ב־DB
        PdfDocument pdfDocument = new PdfDocument();
        pdfDocument.setFilename(filename);
        pdfDocument.setContent(text);
        pdfDocumentRepository.save(pdfDocument);

        // אפשר לשלוח את ה-chunks ל-VectorDB כאן בעתיד
        return pdfDocument;
    }
}
