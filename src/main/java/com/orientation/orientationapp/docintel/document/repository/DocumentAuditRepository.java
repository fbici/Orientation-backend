package com.orientation.orientationapp.docintel.document.repository;

import com.orientation.orientationapp.docintel.document.entity.DocumentAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DocumentAuditRepository extends JpaRepository<DocumentAudit, UUID> {
    Page<DocumentAudit> findByDocumentIdOrderByPerformedAtDesc(UUID documentId, Pageable pageable);
}
