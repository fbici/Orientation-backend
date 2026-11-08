package com.orientation.orientationapp.docintel.document.entity;
import com.orientation.orientationapp.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.time.Instant;
import java.math.BigDecimal;
@Entity @Table(name="doc_extracted_facts") @Getter @Setter @NoArgsConstructor
public class ExtractedFact extends BaseEntity {
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="document_id",nullable=false)
 private Document document;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="document_version_id",nullable=false)
 private DocumentVersion documentVersion;
 @Column(nullable=false) private Integer pageNumber;
 @Column(nullable=false,length=64) private String sourceKey;
 @Column(nullable=false,columnDefinition="text") private String sourcePassage;
 @Column(nullable=false,length=40) private String kind;
 @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
 @Column(nullable=false,columnDefinition="jsonb") private String payload;
 @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
 @Column(nullable=false,columnDefinition="jsonb") private String originalPayload;
 @Column(length=40) private String originalKind;
 private UUID academicYearId;
 @Column(precision=5,scale=2) private BigDecimal confidence;
 @Column(nullable=false,length=20) private String status="EXTRACTED";
 private UUID reviewedBy;
 private Instant reviewedAt;
 @Column(columnDefinition="text") private String reviewNote;
 private UUID publishedEntityId;
 @Column(length=50) private String publishedEntityType;
 private Instant publishedAt;
 private UUID guideVersionId;
}
