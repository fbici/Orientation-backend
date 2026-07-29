package com.orientation.orientationapp.docintel.document.service;

import com.orientation.orientationapp.docintel.document.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

public interface DocumentService {
    Document upload(MultipartFile file, String title, String description, String tenantId, String userId);
    Document getById(UUID id);
    Page<Document> list(String search, String tenantId, Document.DocumentType type, Pageable pageable);
    void delete(UUID id);
    Map<String, Object> getStatistics(String tenantId);
    byte[] download(UUID id);
}
