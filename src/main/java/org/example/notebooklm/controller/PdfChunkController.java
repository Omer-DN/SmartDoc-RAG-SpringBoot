package org.example.notebooklm.controller;

import org.example.notebooklm.model.PdfChunk;
import org.example.notebooklm.model.PdfDocument;
import org.example.notebooklm.repository.PdfDocumentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pdf")
public class PdfChunkController {

    private final PdfDocumentRepository pdfDocumentRepository;

    public PdfChunkController(PdfDocumentRepository pdfDocumentRepository) {
        this.pdfDocumentRepository = pdfDocumentRepository;
    }

    @GetMapping("/{id}/chunks")
    public ResponseEntity<List<PdfChunk>> getPdfChunks(@PathVariable Long id) {
        return pdfDocumentRepository.findById(id)
                .map(pdfDocument -> ResponseEntity.ok(pdfDocument.getChunks()))
                .orElse(ResponseEntity.notFound().build());
    }
}
