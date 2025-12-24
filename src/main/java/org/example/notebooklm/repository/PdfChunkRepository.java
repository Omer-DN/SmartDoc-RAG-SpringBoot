package org.example.notebooklm.repository;

import org.example.notebooklm.model.PdfChunk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PdfChunkRepository extends JpaRepository<PdfChunk, Long> {
}
