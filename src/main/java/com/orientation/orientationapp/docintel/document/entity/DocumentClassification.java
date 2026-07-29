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
@Table(name = "doc_document_classifications")
public class DocumentClassification extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_doc_classification_document"))
    private Document document;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Document.DocumentType primaryType;

    @Column(precision = 5, scale = 2)
    private BigDecimal primaryConfidence;

    @Column(columnDefinition = "jsonb")
    private String allClassificationsJson;

    @Column(length = 100)
    private String classificationEngine;

    @Column(columnDefinition = "jsonb")
    private String featuresJson;

    @Column(precision = 5, scale = 2)
    private BigDecimal classificationScore;
}
