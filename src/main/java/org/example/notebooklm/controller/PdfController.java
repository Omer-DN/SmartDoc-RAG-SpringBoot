package org.example.notebooklm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.notebooklm.model.PdfChunk;
import org.example.notebooklm.model.PdfDocument;
import org.example.notebooklm.service.PdfService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/pdf")
@Tag(name = "PDF Management", description = "Upload, list, view and delete PDF documents")
public class PdfController {

    private final PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @Operation(summary = "Upload a PDF file")
    @ApiResponse(responseCode = "200", description = "PDF uploaded successfully")
    @ApiResponse(responseCode = "400", description = "Invalid PDF file")
    @ApiResponse(responseCode = "500", description = "Error processing PDF")
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body("Please upload a valid PDF file");
        }

        try {
            pdfService.savePdf(file.getBytes(), file.getOriginalFilename());
            return ResponseEntity.ok("PDF uploaded, document + chunks saved");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing PDF");
        }
    }

    @Operation(summary = "Get list of all uploaded PDFs")
    @ApiResponse(responseCode = "200", description = "List of PDFs returned")
    @GetMapping
    public List<PdfDocument> getAllPdfs() {
        return pdfService.getAllPdfs();
    }

    @Operation(summary = "Get all chunks for a specific PDF")
    @ApiResponse(responseCode = "200", description = "List of chunks returned")
    @ApiResponse(responseCode = "404", description = "PDF not found")
    @GetMapping("/{id}/chunks")
    public ResponseEntity<?> getChunks(@PathVariable Long id) {
        List<PdfChunk> chunks = pdfService.getChunksForPdf(id);

        if (chunks == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(chunks);
    }

    @Operation(summary = "Delete a PDF and its chunks")
    @ApiResponse(responseCode = "200", description = "PDF deleted successfully")
    @ApiResponse(responseCode = "404", description = "PDF not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePdf(@PathVariable Long id) {
        boolean deleted = pdfService.deletePdf(id);
        if (deleted) {
            return ResponseEntity.ok("PDF and its chunks deleted successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
