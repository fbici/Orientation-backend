package com.orientation.orientationapp.docintel.document.entity;

import com.orientation.orientationapp.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "doc_document_extractions")
public class DocumentExtraction extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_doc_extraction_document"))
    private Document document;

    @Column(columnDefinition = "text")
    private String fullText;

    @Column(columnDefinition = "text")
    private String cleanedText;

    @Column(precision = 5, scale = 2)
    private BigDecimal ocrConfidence;

    @Column(length = 10)
    private String detectedLanguage;

    private Integer blockCount;

    private Integer paragraphCount;

    private Integer tableCount;

    private Integer imageCount;

    @Column(columnDefinition = "jsonb")
    private String tablesJson;

    @Column(columnDefinition = "jsonb")
    private String structureJson;

    @Column(precision = 5, scale = 2)
    private BigDecimal extractionScore;

    @Column(precision = 5, scale = 2)
    private BigDecimal qualityScore;

    @Column(length = 50)
    private String ocrEngine;
}
