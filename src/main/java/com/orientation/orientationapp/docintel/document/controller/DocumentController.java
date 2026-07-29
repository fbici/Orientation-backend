package com.orientation.orientationapp.docintel.document.controller;

import com.orientation.orientationapp.docintel.document.dto.response.DocumentResponse;
import com.orientation.orientationapp.docintel.document.dto.response.MessageResponse;
import com.orientation.orientationapp.docintel.document.entity.Document;
import com.orientation.orientationapp.docintel.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<DocumentResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description) {

        Document document = documentService.upload(file, title, description, "default", "current-user");
        return ResponseEntity.ok(mapToResponse(document));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<DocumentResponse> getById(@PathVariable UUID id) {
        Document document = documentService.getById(id);
        return ResponseEntity.ok(mapToResponse(document));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Page<DocumentResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) Document.DocumentType type,
            Pageable pageable) {
        Page<Document> documents = documentService.list(search, tenantId, type, pageable);
        return ResponseEntity.ok(documents.map(this::mapToResponse));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Page<DocumentResponse>> search(
            @RequestParam String q,
            Pageable pageable) {
        Page<Document> documents = documentService.list(q, null, null, pageable);
        return ResponseEntity.ok(documents.map(this::mapToResponse));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(documentService.getStatistics(null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<MessageResponse> delete(@PathVariable UUID id) {
        documentService.delete(id);
        return ResponseEntity.ok(MessageResponse.success("Document deleted"));
    }

    private DocumentResponse mapToResponse(Document doc) {
        return DocumentResponse.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .originalFileName(doc.getOriginalFileName())
                .mimeType(doc.getMimeType())
                .fileSize(doc.getFileSize())
                .checksum(doc.getChecksum())
                .documentType(doc.getDocumentType().name())
                .status(doc.getStatus())
                .tenantId(doc.getTenantId())
                .uploadedBy(doc.getUploadedBy())
                .uploadedAt(doc.getUploadedAt())
                .description(doc.getDescription())
                .language(doc.getLanguage())
                .pageCount(doc.getPageCount())
                .ocrScore(doc.getOcrScore())
                .qualityScore(doc.getQualityScore())
                .createdAt(doc.getCreatedAt())
                .build();
    }
}
