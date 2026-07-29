package com.orientation.orientationapp.docintel.document.repository;

import com.orientation.orientationapp.docintel.document.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID>, JpaSpecificationExecutor<Document> {
    Page<Document> findByTenantId(String tenantId, Pageable pageable);
    Page<Document> findByDocumentType(Document.DocumentType type, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.deleted = false AND (:search IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Document> search(@Param("search") String search, Pageable pageable);

    long countByTenantId(String tenantId);
    long countByDocumentType(Document.DocumentType type);
}
