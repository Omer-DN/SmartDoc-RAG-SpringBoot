package org.example.notebooklm.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class responsible only for extracting text from PDF files.
 * Keeps PdfController and PdfService clean and SOLID.
 */
@Component
public class PdfTextExtractor {

    public String extractText(MultipartFile file) throws IOException {
        try (InputStream input = file.getInputStream();
             PDDocument document = PDDocument.load(input)) {

            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}
