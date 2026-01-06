package org.example.notebooklm.repository;

import org.example.notebooklm.model.PdfChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PdfChunkRepository extends JpaRepository<PdfChunk, Long> {

    /**
     * מוצא את כל הצ'אנקים השייכים למסמך מסוים.
     * הערה: וודא שבתוך מחלקת PdfChunk השדה נקרא pdfDocument.
     */
    List<PdfChunk> findByPdfDocumentId(Long pdfDocumentId);

    /**
     * מבצע חיפוש דמיון וקטורי (Cosine Similarity).
     * אנחנו משתמשים ב-Native Query כדי לעקוף את בעיית הטיפוסים של Hibernate
     * ומבצעים CAST מפורש ל-vector.
     */
    @Query(value = "SELECT * FROM pdf_chunks c " +
            "WHERE c.pdf_id = :pdfId " +
            "ORDER BY c.embedding <=> CAST(:vectorString AS vector) " +
            "LIMIT :limit",
            nativeQuery = true)
    List<PdfChunk> findSimilarChunks(@Param("pdfId") Long pdfId,
                                     @Param("vectorString") String vectorString,
                                     @Param("limit") int limit);
}