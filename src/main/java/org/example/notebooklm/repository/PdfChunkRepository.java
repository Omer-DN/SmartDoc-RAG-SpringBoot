package org.example.notebooklm.repository;

import org.example.notebooklm.model.PdfChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PdfChunkRepository extends JpaRepository<PdfChunk, Long> {

    List<PdfChunk> findByPdfDocumentId(Long pdfDocumentId);

    @Query(value = """
        SELECT *
        FROM pdf_chunks
        WHERE pdf_document_id = :pdfId
        ORDER BY embedding <=> CAST(:queryVector AS vector)
        LIMIT :topK
        """,
            nativeQuery = true)
    List<PdfChunk> findSimilarChunks(
            @Param("pdfId") Long pdfId,
            @Param("queryVector") String queryVector,
            @Param("topK") int topK
    );
}
