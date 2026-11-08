package com.orientation.orientationapp.docintel.document.service.impl;

import com.orientation.orientationapp.docintel.classification.engine.DocumentClassifier;
import com.orientation.orientationapp.docintel.classification.model.ClassificationResult;
import com.orientation.orientationapp.docintel.document.entity.*;
import com.orientation.orientationapp.docintel.document.repository.*;
import com.orientation.orientationapp.docintel.document.service.DocumentService;
import com.orientation.orientationapp.docintel.ocr.engine.impl.OcrEngineRegistry;
import com.orientation.orientationapp.docintel.ocr.model.OcrResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final com.orientation.orientationapp.security.service.ResourceAccess access;
    private final com.orientation.orientationapp.storage.service.StorageService storage;
    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentPageRepository documentPageRepository;
    private final DocumentMetadataRepository documentMetadataRepository;
    private final DocumentExtractionRepository documentExtractionRepository;
    private final DocumentClassificationRepository documentClassificationRepository;
    private final DocumentAuditRepository documentAuditRepository;
    private final OcrEngineRegistry ocrEngineRegistry;
    private final DocumentClassifier documentClassifier;

    @Override
    @Transactional
    public Document upload(MultipartFile file, String title, String description, String tenantId, String userId) {
        access.require("DOCUMENT_MANAGE");storage.validate(file);
        var current=access.current();
        String original=file.getOriginalFilename()==null?"document":file.getOriginalFilename();
        original=original.replaceAll("[\\r\\n\\p{Cntrl}]","_");
        if(original.length()>200) original=original.substring(0,200);
        String safeTitle=title==null || title.isBlank()?original:title.strip();
        if(safeTitle.length()>500 || (description!=null && description.length()>20000))
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,"Document metadata exceeds its limit");
        try {
            Document document=Document.builder().title(safeTitle).originalFileName(original)
                .storedFileName(storage.store(file)).mimeType(file.getContentType()).fileSize(file.getSize())
                .checksum(calculateChecksum(file.getBytes())).status("UPLOADED")
                .tenantId(access.tenant(null).toString()).uploadedBy(current.getId().toString())
                .uploadedAt(Instant.now()).description(description).build();
            documentRepository.saveAndFlush(document);
            documentVersionRepository.saveAndFlush(DocumentVersion.builder().document(document).versionNumber(1)
                .storedFileName(document.getStoredFileName()).fileSize(document.getFileSize()).checksum(document.getChecksum())
                .uploadedBy(current.getId().toString()).uploadedAt(Instant.now()).active(true).build());
            documentAuditRepository.save(DocumentAudit.builder().document(document).action("UPLOAD")
                .userId(current.getId().toString()).performedAt(Instant.now()).details("Source stored; processing not yet run").build());
            return document;
        } catch(IOException ex) {
            throw new com.orientation.orientationapp.exception.BusinessException("Document storage failed","STORAGE_ERROR");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Document getById(UUID id) {
        Document document=documentRepository.findById(id)
                .orElseThrow(com.orientation.orientationapp.security.service.ResourceAccess::denied);
        access.document(document.getTenantId(),document.getUploadedBy(),false);
        return document;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Document> list(String search, String tenantId, Document.DocumentType type, Pageable pageable) {
        UUID tenant=access.tenant(tenantId==null ? null : UUID.fromString(tenantId));
        String owner=access.current().getId().toString();
        boolean tenantReader=access.authority("DOCUMENT_READ");
        return documentRepository.findAll((root,query,cb) -> {
            var conditions=new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            conditions.add(cb.equal(root.get("tenantId"),tenant.toString()));
            if (!tenantReader) conditions.add(cb.equal(root.get("uploadedBy"),owner));
            if (type!=null) conditions.add(cb.equal(root.get("documentType"),type));
            if (search!=null && !search.isBlank()) conditions.add(cb.like(cb.lower(root.get("title")),"%"+search.toLowerCase()+"%"));
            return cb.and(conditions.toArray(jakarta.persistence.criteria.Predicate[]::new));
        },pageable);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Document document = getById(id);
        access.document(document.getTenantId(),document.getUploadedBy(),true);
        document.setDeleted(true);
        documentRepository.save(document);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics(String tenantId) {
        var scoped=list(null,tenantId,null,org.springframework.data.domain.Pageable.unpaged()).getContent();
        Map<String,Object> stats=new HashMap<>();
        stats.put("totalDocuments",scoped.size());
        stats.put("documentsByType",Arrays.stream(Document.DocumentType.values()).collect(
            java.util.stream.Collectors.toMap(Enum::name,t -> scoped.stream().filter(d -> d.getDocumentType()==t).count())));
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] download(UUID id) {
        Document document=getById(id);
        try { return java.nio.file.Files.readAllBytes(storage.load(document.getStoredFileName())); }
        catch (java.io.IOException ex) { throw new com.orientation.orientationapp.exception.BusinessException("Stored document unavailable","STORAGE_ERROR"); }
    }

    private String calculateChecksum(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
