package org.example.notebooklm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.notebooklm.model.PdfChunk;
import org.example.notebooklm.model.PdfDocument;
import org.example.notebooklm.service.PdfService;
import org.example.notebooklm.service.RagService;
import org.example.notebooklm.util.PdfTextExtractor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pdf")
@Tag(name = "PDF Management")
public class PdfController {

    private final PdfService pdfService;
    private final RagService ragService;
    private final PdfTextExtractor pdfTextExtractor;

    public PdfController(PdfService pdfService,
                         RagService ragService,
                         PdfTextExtractor pdfTextExtractor) {
        this.pdfService = pdfService;
        this.ragService = ragService;
        this.pdfTextExtractor = pdfTextExtractor;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPdf(@RequestParam("file") MultipartFile file) {
        try {
            String fullText = pdfTextExtractor.extractText(file);

            PdfDocument document = new PdfDocument();
            document.setFilename(file.getOriginalFilename());
            document = pdfService.saveDocument(document);

            pdfService.processPdf(document, fullText);

            return ResponseEntity.ok(Map.of(
                    "message", "PDF uploaded and processed",
                    "documentId", document.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing PDF");
        }
    }

    @GetMapping
    public List<PdfDocument> getAllPdfs() {
        return pdfService.getAllPdfs();
    }

    @GetMapping("/{id}/chunks")
    public List<PdfChunk> getChunks(@PathVariable Long id) {
        return pdfService.getChunksForPdf(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePdf(@PathVariable Long id) {
        boolean deleted = pdfService.deletePdf(id);
        return deleted ? ResponseEntity.ok("Deleted") : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/ask")
    public ResponseEntity<?> ask(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String question = body.get("question");
        String answer = ragService.answerQuestion(id, question);
        return ResponseEntity.ok(Map.of("answer", answer));
    }
}
