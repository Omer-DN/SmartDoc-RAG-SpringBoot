package org.example.notebooklm.test;


import org.example.notebooklm.service.GeminiEmbeddingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GeminiEmbeddingServiceTest {

    @Autowired
    private GeminiEmbeddingService geminiEmbeddingService;

    @Test
    void testEmbeddingNotNullAndLength() {
        String text = "Hello world";

        float[] embedding = geminiEmbeddingService.generateEmbedding(text);

        assertNotNull(embedding, "Embedding should not be null");
        assertTrue(embedding.length > 0, "Embedding length should be greater than 0");

        System.out.println("Embedding length: " + embedding.length);
        System.out.println("Sample values: " + Arrays.toString(Arrays.copyOf(embedding, 10)));
    }

    @Test
    void testEmbeddingConsistency() {
        String text = "Hello world";

        float[] emb1 = geminiEmbeddingService.generateEmbedding(text);
        float[] emb2 = geminiEmbeddingService.generateEmbedding(text);

        assertEquals(emb1.length, emb2.length, "Embeddings length should be equal");

        // חישוב דמיון קוסינוס פשוט
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < emb1.length; i++) {
            dot += emb1[i] * emb2[i];
            normA += emb1[i] * emb1[i];
            normB += emb2[i] * emb2[i];
        }
        double cosineSim = dot / (Math.sqrt(normA) * Math.sqrt(normB));
        System.out.println("Cosine similarity: " + cosineSim);

        assertTrue(cosineSim > 0.99, "Cosine similarity should be very high for same text");
    }
}
