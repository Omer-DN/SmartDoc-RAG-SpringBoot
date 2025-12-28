package org.example.notebooklm.repository;

import org.example.notebooklm.model.PdfChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PdfChunkRepository extends JpaRepository<PdfChunk, Long> {

    List<PdfChunk> findByPdfDocumentId(Long pdfDocumentId);

}