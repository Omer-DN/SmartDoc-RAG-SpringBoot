package org.example.notebooklm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.notebooklm.model.PdfChunk;
import org.example.notebooklm.model.PdfDocument;
import org.example.notebooklm.service.PdfService;
import org.example.notebooklm.service.RagService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pdf")
@Tag(name = "PDF Management", description = "Upload, list, view, delete PDFs and ask questions")
public class PdfController {

    private final PdfService pdfService;
    private final RagService ragService; // שירות ה-RAG שלנו

    public PdfController(PdfService pdfService, RagService ragService) {
        this.pdfService = pdfService;
        this.ragService = ragService;
    }

    // ===========================
    // Upload PDF
    // ===========================
    @Operation(summary = "Upload a PDF file")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPdf(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body("Please upload a valid PDF file");
        }

        try {
            PdfDocument doc = pdfService.processPdf(file);
            return ResponseEntity.ok(Map.of(
                    "message", "PDF uploaded successfully",
                    "documentId", doc.getId(),
                    "filename", doc.getFilename()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing PDF");
        }
    }

    // ===========================
    // Get all PDFs
    // ===========================
    @Operation(summary = "Get list of all uploaded PDFs")
    @GetMapping
    public List<PdfDocument> getAllPdfs() {
        return pdfService.getAllPdfs();
    }

    // ===========================
    // Get chunks of a PDF
    // ===========================
    @Operation(summary = "Get all chunks for a specific PDF")
    @GetMapping("/{id}/chunks")
    public ResponseEntity<?> getChunks(@PathVariable Long id) {
        List<PdfChunk> chunks = pdfService.getChunksForPdf(id);

        if (chunks == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(chunks);
    }

    // ===========================
    // Delete PDF
    // ===========================
    @Operation(summary = "Delete a PDF and its chunks")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePdf(@PathVariable Long id) {
        boolean deleted = pdfService.deletePdf(id);
        if (deleted) {
            return ResponseEntity.ok("PDF and its chunks deleted successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ===========================
    // Reset all PDFs and chunks
    // ===========================
    @DeleteMapping("/reset")
    public ResponseEntity<String> reset() {
        pdfService.resetAll();
        return ResponseEntity.ok("All documents and chunks deleted.");
    }

    // ===========================
    // RAG: Ask a question about a PDF
    // ===========================
    @Operation(summary = "Ask a question about a PDF")
    @PostMapping("/{id}/ask")
    public ResponseEntity<?> askQuestion(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String question = body.get("question");
        if (question == null || question.isEmpty()) {
            return ResponseEntity.badRequest().body("Question cannot be empty");
        }

        try {
            String answer = ragService.answerQuestion(id, question);
            return ResponseEntity.ok(Map.of(
                    "question", question,
                    "answer", answer
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error answering question");
        }
    }
}
