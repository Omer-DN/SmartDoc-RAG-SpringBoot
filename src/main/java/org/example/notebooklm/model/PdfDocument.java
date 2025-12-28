package org.example.notebooklm.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pdf_documents")
public class PdfDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "pdfDocument", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<PdfChunk> chunks = new ArrayList<>();

    // ===== helpers =====
    public void addChunk(PdfChunk chunk) {
        chunks.add(chunk);
        chunk.setPdfDocument(this);
    }

    public void removeChunk(PdfChunk chunk) {
        chunks.remove(chunk);
        chunk.setPdfDocument(null);
    }

    // ===== getters & setters =====
    public Long getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<PdfChunk> getChunks() {
        return chunks;
    }

    public void setChunks(List<PdfChunk> chunks) {
        this.chunks = chunks;
    }
}
