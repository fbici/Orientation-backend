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
        try {
            String checksum = calculateChecksum(file.getBytes());

            Document document = Document.builder()
                    .title(title != null ? title : file.getOriginalFilename())
                    .originalFileName(file.getOriginalFilename())
                    .storedFileName(UUID.randomUUID() + "_" + file.getOriginalFilename())
                    .mimeType(file.getContentType())
                    .fileSize(file.getSize())
                    .checksum(checksum)
                    .status("PROCESSING")
                    .tenantId(tenantId)
                    .uploadedBy(userId)
                    .uploadedAt(Instant.now())
                    .description(description)
                    .build();

            document = documentRepository.save(document);

            // Create version
            DocumentVersion version = DocumentVersion.builder()
                    .document(document)
                    .versionNumber(1)
                    .storedFileName(document.getStoredFileName())
                    .fileSize(document.getFileSize())
                    .checksum(document.getChecksum())
                    .uploadedBy(userId)
                    .uploadedAt(Instant.now())
                    .active(true)
                    .build();

            // Perform OCR
            log.info("Performing OCR for document: {}", document.getId());
            var ocrEngine = ocrEngineRegistry.getEngineForMimeType(file.getContentType());
            OcrResult ocrResult = ocrEngine.performOcr(file.getInputStream(), file.getContentType());

            // Store extraction results
            DocumentExtraction extraction = DocumentExtraction.builder()
                    .document(document)
                    .fullText(ocrResult.getRawText())
                    .cleanedText(ocrResult.getCleanedText())
                    .ocrConfidence(BigDecimal.valueOf(ocrResult.getConfidence()))
                    .detectedLanguage(ocrResult.getDetectedLanguage())
                    .blockCount(ocrResult.getBlocks().size())
                    .paragraphCount(ocrResult.getParagraphs().size())
                    .tableCount(ocrResult.getTables().size())
                    .imageCount(ocrResult.getImages().size())
                    .ocrEngine(ocrResult.getEngineUsed())
                    .extractionScore(BigDecimal.valueOf(ocrResult.getConfidence()))
                    .qualityScore(BigDecimal.valueOf(ocrResult.getConfidence() * 100))
                    .build();

            // Store pages
            List<DocumentPage> pages = new ArrayList<>();
            for (OcrResult.OcrPage ocrPage : ocrResult.getPages()) {
                DocumentPage page = DocumentPage.builder()
                        .document(document)
                        .pageNumber(ocrPage.getPageNumber())
                        .rawText(ocrPage.getText())
                        .cleanedText(ocrPage.getText())
                        .ocrConfidence(BigDecimal.valueOf(ocrPage.getConfidence()))
                        .build();
                pages.add(page);
            }

            // Classify document
            Map<String, Object> metadata = Map.of(
                    "title", document.getTitle(),
                    "mimeType", document.getMimeType()
            );
            ClassificationResult classification = documentClassifier.classify(ocrResult.getRawText(), metadata);

            DocumentClassification docClassification = DocumentClassification.builder()
                    .document(document)
                    .primaryType(classification.getPrimaryType())
                    .primaryConfidence(classification.getPrimaryConfidence())
                    .classificationEngine(classification.getClassificationEngine())
                    .classificationScore(classification.getPrimaryConfidence())
                    .build();

            // Store metadata
            DocumentMetadata docMetadata = DocumentMetadata.builder()
                    .document(document)
                    .title(document.getTitle())
                    .pageCount(ocrResult.getPageCount())
                    .language(ocrResult.getDetectedLanguage())
                    .checksum(document.getChecksum())
                    .build();

            // Update document
            document.setDocumentType(classification.getPrimaryType());
            document.setStatus("COMPLETED");
            document.setPageCount(ocrResult.getPageCount());
            document.setOcrScore(BigDecimal.valueOf(ocrResult.getConfidence()));
            document.setQualityScore(BigDecimal.valueOf(ocrResult.getConfidence() * 100));
            document.setLanguage(ocrResult.getDetectedLanguage());

            // Save all
            documentRepository.save(document);
            documentPageRepository.saveAll(pages);
            documentExtractionRepository.save(extraction);
            documentClassificationRepository.save(docClassification);
            documentMetadataRepository.save(docMetadata);

            // Audit
            DocumentAudit audit = DocumentAudit.builder()
                    .document(document)
                    .action("UPLOAD")
                    .userId(userId)
                    .performedAt(Instant.now())
                    .details("Document uploaded and processed")
                    .build();
            documentAuditRepository.save(audit);

            log.info("Document uploaded and processed: {} ({})", document.getTitle(), document.getId());
            return document;

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload document", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Document getById(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Document> list(String search, String tenantId, Document.DocumentType type, Pageable pageable) {
        if (search != null) {
            return documentRepository.search(search, pageable);
        }
        if (tenantId != null) {
            return documentRepository.findByTenantId(tenantId, pageable);
        }
        if (type != null) {
            return documentRepository.findByDocumentType(type, pageable);
        }
        return documentRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Document document = getById(id);
        document.setDeleted(true);
        documentRepository.save(document);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics(String tenantId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDocuments", documentRepository.count());
        stats.put("documentsByType", Arrays.stream(Document.DocumentType.values())
                .collect(java.util.stream.Collectors.toMap(
                        type -> type.name(),
                        type -> documentRepository.countByDocumentType(type)
                )));
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] download(UUID id) {
        // In production, this would read from file storage
        Document document = getById(id);
        throw new RuntimeException("File storage not implemented yet");
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
