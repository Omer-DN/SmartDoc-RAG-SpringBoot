package org.example.notebooklm.controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.example.notebooklm.service.PdfService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadPdf(@RequestPart("file") MultipartFile file) {
        try {
            String fullText = extractText(file);
            // חיתוך גס ל-1000 תווים (ניתן לשפר ללוגיקה חכמה יותר)
            List<String> chunks = List.of(fullText.split("(?<=\\G.{1000})"));

            pdfService.processAndSavePdf(file.getOriginalFilename(), chunks);

            return ResponseEntity.ok("הקובץ עובד בהצלחה!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("שגיאה: " + e.getMessage());
        }
    }

    @GetMapping("/ask")
    public ResponseEntity<String> askQuestion(@RequestParam long pdfId, @RequestParam String query) {
        String answer = pdfService.askQuestion(pdfId, query);
        return ResponseEntity.ok(answer);
    }

    private String extractText(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}