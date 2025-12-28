package org.example.notebooklm.service;

import org.example.notebooklm.model.PdfChunk;
import org.example.notebooklm.model.PdfDocument;
import org.example.notebooklm.repository.PdfChunkRepository;
import org.example.notebooklm.repository.PdfDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

@Service
public class PdfService {

    private final GeminiEmbeddingService geminiEmbeddingService;
    private final PdfDocumentRepository documentRepository;
    private final PdfChunkRepository chunkRepository;

    public PdfService(GeminiEmbeddingService geminiEmbeddingService,
                      PdfDocumentRepository documentRepository,
                      PdfChunkRepository chunkRepository) {
        this.geminiEmbeddingService = geminiEmbeddingService;
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
    }

    public PdfDocument processPdf(MultipartFile file) {
        try {
            String text = extractTextFromPdf(file.getInputStream());

            PdfDocument pdfDocument = new PdfDocument();
            pdfDocument.setFilename(file.getOriginalFilename());
            pdfDocument.setContent(text);

            pdfDocument = documentRepository.save(pdfDocument);

            List<String> chunks = splitIntoChunks(text, 1000);

            for (int i = 0; i < chunks.size(); i++) {
                String chunkText = chunks.get(i);

                double[] embedding = geminiEmbeddingService.generateEmbedding(chunkText);

                if (embedding == null) {
                    System.err.println("Failed to generate embedding for chunk " + i);
                    continue;
                }

                PdfChunk chunk = new PdfChunk();
                chunk.setChunkIndex(i);
                chunk.setText(chunkText);
                chunk.setEmbedding(embedding);

                // חשוב! זה מחבר את ה-chunk למסמך
                pdfDocument.addChunk(chunk);

                chunkRepository.save(chunk);
            }

            return pdfDocument;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to process PDF", e);
        }
    }

    public List<PdfDocument> getAllPdfs() {
        return documentRepository.findAll();
    }

    public List<PdfChunk> getChunksForPdf(Long id) {
        Optional<PdfDocument> doc = documentRepository.findById(id);
        return doc.map(PdfDocument::getChunks).orElse(null);
    }

    public boolean deletePdf(Long id) {
        Optional<PdfDocument> doc = documentRepository.findById(id);

        if (doc.isEmpty()) {
            return false;
        }

        documentRepository.delete(doc.get());
        return true;
    }

    private String extractTextFromPdf(InputStream inputStream) throws Exception {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private List<String> splitIntoChunks(String text, int maxChars) {
        List<String> chunks = new ArrayList<>();

        String[] words = text.split("\\s+");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            if (current.length() + word.length() + 1 > maxChars) {
                chunks.add(current.toString());
                current = new StringBuilder();
            }
            current.append(word).append(" ");
        }

        if (!current.isEmpty()) {
            chunks.add(current.toString());
        }

        return chunks;
    }

    public void resetAll() {
        chunkRepository.deleteAll();
        documentRepository.deleteAll();
    }
}
