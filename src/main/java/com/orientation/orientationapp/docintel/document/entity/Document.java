package com.orientation.orientationapp.docintel.document.entity;

import com.orientation.orientationapp.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "doc_documents")
public class Document extends BaseEntity {

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 200)
    private String originalFileName;

    @Column(nullable = false, length = 255)
    private String storedFileName;

    @Column(nullable = false, length = 100)
    private String mimeType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 500)
    private String checksum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private DocumentType documentType = DocumentType.UNKNOWN;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "UPLOADED";

    @Column(length = 100)
    private String tenantId;

    @Column(length = 100)
    private String organizationId;

    @Column(length = 100)
    private String uploadedBy;

    private Instant uploadedAt;

    @Column(length = 500)
    private String description;

    @Column(columnDefinition = "jsonb")
    private String tags;

    @Column(length = 10)
    private String language;

    private Integer pageCount;

    @Column(precision = 5, scale = 2)
    private BigDecimal ocrScore;

    @Column(precision = 5, scale = 2)
    private BigDecimal qualityScore;

    @OneToMany(mappedBy = "document", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<DocumentVersion> versions = new HashSet<>();

    @OneToMany(mappedBy = "document", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<DocumentPage> pages = new HashSet<>();

    @OneToOne(mappedBy = "document", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private DocumentMetadata metadata;

    @OneToOne(mappedBy = "document", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private DocumentExtraction extraction;

    @OneToOne(mappedBy = "document", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private DocumentClassification classification;

    @OneToMany(mappedBy = "document", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<DocumentAudit> audits = new HashSet<>();

    public enum DocumentType {
        ORIENTATION_GUIDE,
        UNIVERSITY_GUIDE,
        SCHOLARSHIP_GUIDE,
        REGULATION,
        TRANSCRIPT,
        PROGRAM,
        BROCHURE,
        UNKNOWN
    }
}
