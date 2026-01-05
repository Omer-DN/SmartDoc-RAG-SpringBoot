package org.example.notebooklm.service;

import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class SecretService {

    private final String geminiKey;

    public SecretService() {
        try {
            Properties props = new Properties();
            props.load(getClass().getClassLoader().getResourceAsStream("secrets.properties"));
            this.geminiKey = props.getProperty("gemini.key");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Gemini key", e);
        }
    }

    public String getGeminiKey() {
        return geminiKey;
    }
}
