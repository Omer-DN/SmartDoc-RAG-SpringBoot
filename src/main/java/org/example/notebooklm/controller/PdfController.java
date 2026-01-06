package org.example.notebooklm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.example.notebooklm.model.PdfDocument;
import org.example.notebooklm.service.IngestionService;
import org.example.notebooklm.service.PdfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pdf")
@CrossOrigin(origins = "*")
public class PdfController {

    private static final Logger logger = LoggerFactory.getLogger(PdfController.class);
    private final PdfService pdfService;
    private final IngestionService ingestionService;

    public PdfController(PdfService pdfService, IngestionService ingestionService) {
        this.pdfService = pdfService;
        this.ingestionService = ingestionService;
    }

    @Operation(summary = "Upload a PDF file and process its embeddings")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPdf(
            @Parameter(description = "The PDF file to upload", required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")))
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        try (InputStream inputStream = file.getInputStream()) {
            ingestionService.validatePdfFile(file);
            String text = ingestionService.extractTextFromPdf(inputStream);

            logger.info("Received PDF upload request: {}", file.getOriginalFilename());

            PdfDocument pdfDoc = new PdfDocument();
            pdfDoc.setFileName(file.getOriginalFilename());

            PdfDocument savedDoc = pdfService.saveDocument(pdfDoc);
            logger.info("Saved PDF document with ID {}", savedDoc.getId());

            pdfService.processPdf(savedDoc, text);

            return ResponseEntity.ok(savedDoc);

        } catch (IOException e) {
            logger.error("Error processing PDF upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process PDF: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<PdfDocument>> getAllPdfs() {
        return ResponseEntity.ok(pdfService.getAllPdfs());
    }

    @PostMapping("/{id}/ask")
    public ResponseEntity<?> ask(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String question = payload.get("question");
        if (question == null || question.isEmpty()) {
            return ResponseEntity.badRequest().body("Question is missing");
        }

        try {
            String answer = pdfService.askQuestion(id, question);
            return ResponseEntity.ok(Map.of("answer", answer));
        } catch (Exception e) {
            logger.error("Error answering question for PDF {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePdf(@PathVariable Long id) {
        boolean deleted = pdfService.deletePdf(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "PDF deleted successfully"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/reset")
    public ResponseEntity<?> resetAll() {
        pdfService.resetAll();
        return ResponseEntity.ok(Map.of("message", "All documents cleared"));
    }
}