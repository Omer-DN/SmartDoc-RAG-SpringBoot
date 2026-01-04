package org.example.notebooklm.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import com.vladmihalcea.hibernate.type.array.DoubleArrayType;

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

    // ⭐ הפתרון הנכון: Hibernate ממפה double[] ל-vector דרך DoubleArrayType
    @Type(DoubleArrayType.class)
    @Column(columnDefinition = "vector(768)")
    private double[] embedding;

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

    public double[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(double[] embedding) {
        this.embedding = embedding;
    }

    @Transient
    private Double distance;

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }
}
