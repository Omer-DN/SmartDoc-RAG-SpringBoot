package org.example.notebooklm.repository;

import org.example.notebooklm.model.PdfDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PdfDocumentRepository extends JpaRepository<PdfDocument, Long> {

}
