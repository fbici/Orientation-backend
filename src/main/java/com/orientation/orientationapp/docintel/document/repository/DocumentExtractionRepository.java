package com.orientation.orientationapp.docintel.document.repository;

import com.orientation.orientationapp.docintel.document.entity.DocumentExtraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentExtractionRepository extends JpaRepository<DocumentExtraction, UUID> {
    Optional<DocumentExtraction> findByDocumentId(UUID documentId);
}
