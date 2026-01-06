package org.example.notebooklm.exception;

/**
 * Exception thrown when PDF ingestion operations fail.
 */
public class IngestionException extends RuntimeException {
    public IngestionException(String message) {
        super(message);
    }

    public IngestionException(String message, Throwable cause) {
        super(message, cause);
    }
}



