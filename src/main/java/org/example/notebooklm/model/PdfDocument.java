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

    private String fileName;

    @OneToMany(mappedBy = "pdfDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<PdfChunk> chunks = new ArrayList<>();

    // מתודת עזר לניהול הקשר הדו-כיווני
    public void addChunk(PdfChunk chunk) {
        chunks.add(chunk);
        chunk.setPdfDocument(this);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public List<PdfChunk> getChunks() { return chunks; }
    public void setChunks(List<PdfChunk> chunks) { this.chunks = chunks; }
}