package org.example.notebooklm.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.Arrays;

@Entity
@Table(name = "pdf_chunks")
public class PdfChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String text;

    @Column(name = "chunk_index")
    private int chunkIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdf_document_id")
    @JsonBackReference
    private PdfDocument pdfDocument;

    // ✅ embedding נשמר כ-TEXT
    @Lob
    @Column(columnDefinition = "TEXT")
    private String embedding;

    // ===== getters & setters =====

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public PdfDocument getPdfDocument() {
        return pdfDocument;
    }

    public void setPdfDocument(PdfDocument pdfDocument) {
        this.pdfDocument = pdfDocument;
    }

    // מקבל double[] (מ-Gemini) ושומר כ-String
    public void setEmbedding(double[] arr) {
        if (arr == null) {
            this.embedding = null;
            return;
        }
        this.embedding = Arrays.toString(arr);
    }

    // מחזיר חזרה double[] אם צריך
    public double[] getEmbedding() {
        if (embedding == null || embedding.isBlank()) {
            return new double[0];
        }

        return Arrays.stream(
                        embedding.replace("[", "")
                                .replace("]", "")
                                .split(","))
                .map(String::trim)
                .mapToDouble(Double::parseDouble)
                .toArray();
    }
}
