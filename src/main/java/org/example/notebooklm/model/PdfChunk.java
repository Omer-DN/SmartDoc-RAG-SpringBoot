package org.example.notebooklm.model;

import jakarta.persistence.*;

@Entity
@Table(name = "pdf_chunks")
public class PdfChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String text;

    private int chunkIndex;

    @ManyToOne
    @JoinColumn(name = "pdf_id")
    private PdfDocument pdfDocument;

    // הגדרה שתואמת ל-Gemini
    @Column(name = "embedding", columnDefinition = "vector(768)")
    private float[] embedding;

    public PdfChunk() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public int getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(int chunkIndex) { this.chunkIndex = chunkIndex; }
    public PdfDocument getPdfDocument() { return pdfDocument; }
    public void setPdfDocument(PdfDocument pdfDocument) { this.pdfDocument = pdfDocument; }
    public float[] getEmbedding() { return embedding; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }
}