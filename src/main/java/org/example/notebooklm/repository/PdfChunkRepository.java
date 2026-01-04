package org.example.notebooklm.repository;

import org.example.notebooklm.model.PdfChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PdfChunkRepository extends JpaRepository<PdfChunk, Long> {

    List<PdfChunk> findByPdfDocumentId(Long pdfId);

    void deleteByPdfDocumentId(Long pdfId);

    @Query(value = """
            SELECT 
                id,
                text,
                chunk_index,
                pdf_document_id,
                embedding,
                (embedding <-> CAST(:queryVector AS vector)) AS distance
            FROM pdf_chunk
            WHERE pdf_document_id = :pdfId
            ORDER BY embedding <-> CAST(:queryVector AS vector)
            LIMIT :topK
            """,
            nativeQuery = true)
    List<PdfChunk> findSimilarChunks(
            @Param("pdfId") Long pdfId,
            @Param("queryVector") String queryVector,
            @Param("topK") int topK
    );
}
