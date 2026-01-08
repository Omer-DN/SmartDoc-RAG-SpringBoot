package org.example.notebooklm.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel; // וודא שזה המודל של Gemini
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.example.notebooklm.model.PdfChunk;
import org.example.notebooklm.model.PdfDocument;
import org.example.notebooklm.repository.PdfChunkRepository;
import org.example.notebooklm.repository.PdfDocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PdfService {

    private final PdfChunkRepository chunkRepo;
    private final PdfDocumentRepository pdfRepo;
    private final EmbeddingModel embeddingModel;
    private final ChatLanguageModel chatModel;

    public PdfService(PdfChunkRepository chunkRepo,
                      PdfDocumentRepository pdfRepo,
                      @Value("${google.ai.api.key}") String apiKey) {
        this.chunkRepo = chunkRepo;
        this.pdfRepo = pdfRepo;

        // שימוש במודל ה-Embeddings של Gemini (מייצר 768 ממדים)
        this.embeddingModel = GoogleAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("embedding-001")
                .build();

        this.chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-1.5-flash")
                .build();
    }

    public void processAndSavePdf(String fileName, List<String> textChunks) {
        PdfDocument doc = pdfRepo.save(new PdfDocument(fileName));

        for (int i = 0; i < textChunks.size(); i++) {
            String text = textChunks.get(i);
            // כאן נוצר הוקטור (float[])
            float[] vector = embeddingModel.embed(text).content().vector();

            PdfChunk chunk = new PdfChunk();
            chunk.setText(text);
            chunk.setChunkIndex(i);
            chunk.setPdfDocument(doc);

            // קריאה למתודה הנכונה ב-Repository
            chunkRepo.saveChunk(chunk, vector);

        }
    }

    public String askQuestion(long pdfId, String query) {
        // הפיכת השאלה (String) לוקטור (float[])
        float[] queryVector = embeddingModel.embed(query).content().vector();

        // עכשיו findSimilarChunks מקבל float[] כפי שנדרש
        List<PdfChunk> chunks = chunkRepo.findSimilarChunks(pdfId, queryVector, 5);

        String context = chunks.stream().map(PdfChunk::getText).collect(Collectors.joining("\n---\n"));
        return chatModel.generate("ענה בעברית על סמך הטקסט:\n" + context + "\nשאלה: " + query);
    }

}