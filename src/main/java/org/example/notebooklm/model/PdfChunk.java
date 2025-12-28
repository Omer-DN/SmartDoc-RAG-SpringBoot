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

    @Column(columnDefinition = "text")
    private String text;

    @Column(name = "chunk_index")
    private int chunkIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdf_document_id")
    @JsonBackReference
    private PdfDocument pdfDocument;

    // נשמר ב-Postgres כ-vector(768), ב-Java כמערך double[]
    @Column(columnDefinition = "vector(768)")
    private String embedding;

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

    // setter שמתאים לשורה chunk.setEmbedding(embedding);
    public void setEmbedding(double[] arr) {
        this.embedding = Arrays.toString(arr)
                .replace("[", "")
                .replace("]", "");
    }

    // getter שמחזיר double[] מה-DB
    public double[] getEmbedding() {
        if (embedding == null || embedding.isBlank()) {
            return new double[0];
        }
        return Arrays.stream(embedding.split(","))
                .map(String::trim)
                .mapToDouble(Double::parseDouble)
                .toArray();
    }
}
