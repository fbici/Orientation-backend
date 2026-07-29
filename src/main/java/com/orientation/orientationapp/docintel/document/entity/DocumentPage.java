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
@Table(name = "doc_document_pages")
public class DocumentPage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false, foreignKey = @ForeignKey(name = "fk_doc_page_document"))
    private Document document;

    @Column(nullable = false)
    private Integer pageNumber;

    @Column(columnDefinition = "text")
    private String rawText;

    @Column(columnDefinition = "text")
    private String cleanedText;

    @Column(precision = 5, scale = 2)
    private BigDecimal ocrConfidence;

    @Column(length = 10)
    private String detectedLanguage;

    @Column(columnDefinition = "jsonb")
    private String blocksJson;

    @Column(columnDefinition = "jsonb")
    private String paragraphsJson;

    @Column(columnDefinition = "jsonb")
    private String tablesJson;

    @Column(columnDefinition = "jsonb")
    private String imagesJson;

    private Integer width;

    private Integer height;
}
