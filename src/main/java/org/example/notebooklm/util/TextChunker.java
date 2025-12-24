package org.example.notebooklm.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TextChunker {

    public List<String> splitText(String text, int chunkSize) {

        List<String> chunks = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return chunks;
        }

        int length = text.length();

        for (int start = 0; start < length; start += chunkSize) {
            int end = Math.min(length, start + chunkSize);
            String chunk = text.substring(start, end).trim();

            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }
        }

        return chunks;
    }
}
