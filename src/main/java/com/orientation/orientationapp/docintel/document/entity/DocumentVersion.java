package com.orientation.orientationapp.docintel.document.entity;

import com.orientation.orientationapp.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "doc_document_versions")
public class DocumentVersion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false, foreignKey = @ForeignKey(name = "fk_doc_version_document"))
    private Document document;

    @Column(nullable = false)
    private Integer versionNumber;

    @Column(nullable = false, length = 500)
    private String storedFileName;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 500)
    private String checksum;

    @Column(length = 100)
    private String uploadedBy;

    @Column(length = 500)
    private String changeDescription;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(nullable = false)
    private Instant uploadedAt;
}
