package org.example.notebooklm.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.notebooklm.model.PdfChunk;
import org.example.notebooklm.model.PdfDocument;
import org.example.notebooklm.service.PdfService;
import org.example.notebooklm.service.RagService;
import org.example.notebooklm.util.PdfTextExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(PdfController.class);

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
        logger.info("Received PDF upload request: {}", file.getOriginalFilename());

        try {
            String fullText = pdfTextExtractor.extractText(file);
            logger.info("Extracted {} characters from PDF {}", fullText.length(), file.getOriginalFilename());

            PdfDocument document = new PdfDocument();
            document.setFilename(file.getOriginalFilename());
            document = pdfService.saveDocument(document);

            logger.info("Saved PDF document with ID {}", document.getId());

            pdfService.processPdf(document, fullText);

            logger.info("Finished processing PDF {}", document.getId());

            return ResponseEntity.ok(Map.of(
                    "message", "PDF uploaded and processed",
                    "documentId", document.getId()
            ));
        } catch (Exception e) {
            logger.error("Error processing PDF {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(500).body("Error processing PDF");
        }
    }

    @GetMapping
    public List<PdfDocument> getAllPdfs() {
        logger.info("Fetching all PDFs");
        return pdfService.getAllPdfs();
    }

    @GetMapping("/{id}/chunks")
    public List<PdfChunk> getChunks(@PathVariable Long id) {
        logger.info("Fetching chunks for PDF {}", id);
        return pdfService.getChunksForPdf(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePdf(@PathVariable Long id) {
        logger.info("Deleting PDF {}", id);
        boolean deleted = pdfService.deletePdf(id);
        return deleted ? ResponseEntity.ok("Deleted") : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/ask")
    public ResponseEntity<?> ask(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String question = body.get("question");
        logger.info("Received question for PDF {}: {}", id, question);

        String answer = ragService.answerQuestion(id, question);

        logger.info("Answer for PDF {}: {}", id, answer);

        return ResponseEntity.ok(Map.of("answer", answer));
    }
}
