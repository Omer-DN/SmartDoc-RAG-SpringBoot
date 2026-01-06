package org.example.notebooklm.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Properties;

@Service
public class SecretService {

    private final String geminiKey;

    public SecretService(@Value("${google.api.key:}") String apiKeyFromProps,
                         Environment environment) {
        String key = apiKeyFromProps;

        // Fallback to secrets.properties on classpath (optional)
        if (key == null || key.isBlank()) {
            key = loadFromSecretsFile();
        }

        // Final guard: also allow env var GOOGLE_API_KEY
        if (key == null || key.isBlank()) {
            key = environment.getProperty("GOOGLE_API_KEY");
        }

        if (key == null || key.isBlank()) {
            throw new RuntimeException("Gemini API key is missing. Provide google.api.key or secrets.properties or env GOOGLE_API_KEY.");
        }

        this.geminiKey = key;
    }

    public String getGeminiKey() {
        return geminiKey;
    }

    private String loadFromSecretsFile() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("secrets.properties")) {
            if (is == null) {
                return null;
            }
            Properties props = new Properties();
            props.load(is);
            return props.getProperty("gemini.key");
        } catch (Exception e) {
            return null;
        }
    }
}
