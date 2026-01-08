package org.example.notebooklm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pdf_documents")
public class PdfDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // השדה שהיה חסר וגרם לשגיאה ב-Repository
    private LocalDateTime uploadedAt;

    @OneToMany(mappedBy = "pdfDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PdfChunk> chunks = new ArrayList<>();

    public PdfDocument() {
        this.uploadedAt = LocalDateTime.now(); // מגדיר זמן נוכחי כברירת מחדל
    }

    public PdfDocument(String name) {
        this();
        this.name = name;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public List<PdfChunk> getChunks() { return chunks; }
    public void setChunks(List<PdfChunk> chunks) { this.chunks = chunks; }
}