package org.example.notebooklm.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {

    private static final int CHUNK_SIZE = 800;

    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();

        for (int i = 0; i < text.length(); i += CHUNK_SIZE) {
            int end = Math.min(text.length(), i + CHUNK_SIZE);
            chunks.add(text.substring(i, end));
        }

        return chunks;
    }
}
