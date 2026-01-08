package org.example.notebooklm.repository;

import org.example.notebooklm.model.PdfDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PdfDocumentRepository extends JpaRepository<PdfDocument, Long> {

    // עכשיו Spring ימצא את uploadedAt במודל ולא יקרוס
    Optional<PdfDocument> findTopByOrderByUploadedAtDesc();
}