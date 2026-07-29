package com.orientation.orientationapp.docintel.document.repository;

import com.orientation.orientationapp.docintel.document.entity.DocumentClassification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentClassificationRepository extends JpaRepository<DocumentClassification, UUID> {
    Optional<DocumentClassification> findByDocumentId(UUID documentId);
}
