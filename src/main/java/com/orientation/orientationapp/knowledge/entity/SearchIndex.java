package com.orientation.orientationapp.knowledge.entity;
import java.util.UUID;

import com.orientation.orientationapp.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "knowledge_search_index")
public class SearchIndex extends BaseEntity {
    private java.util.UUID nodeId;
    @Column(length=100) private String nodeType;
    private java.util.UUID tenantId;
    private java.util.UUID programId;
    private java.util.UUID universityId;
    private java.util.UUID countryId;
    private java.util.UUID academicYearId;
    private java.util.UUID sourceDocumentId;
    private java.util.UUID guideVersionId;
    @Column(nullable=false) @Builder.Default private Boolean verified=false;


    @Column(nullable = false, length = 100)
    private String entityType;

    @Column(nullable = false)
    private UUID entityId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "text")
    private String content;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;

    @Column(columnDefinition = "text")
    private String embedding;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
