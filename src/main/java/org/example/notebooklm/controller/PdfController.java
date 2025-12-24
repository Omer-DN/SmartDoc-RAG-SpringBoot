package org.example.notebooklm.controller;

import org.example.notebooklm.model.PdfDocument;
import org.example.notebooklm.repository.PdfDocumentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfDocumentRepository pdfDocumentRepository;

    public PdfController(PdfDocumentRepository pdfDocumentRepository) {
        this.pdfDocumentRepository = pdfDocumentRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body("Please upload a valid PDF file");
        }

        try {
            // המרת PDF לטקסט
            PDDocument document = PDDocument.load(file.getInputStream());
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();

            // שמירה ב-DB

            PdfDocument pdfDocument = new PdfDocument();
            pdfDocument.setFilename(file.getOriginalFilename());
            pdfDocument.setContent(text);
            pdfDocumentRepository.save(pdfDocument);

            return ResponseEntity.ok("File uploaded and saved: " + file.getOriginalFilename());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing PDF");
        }
    }
}
