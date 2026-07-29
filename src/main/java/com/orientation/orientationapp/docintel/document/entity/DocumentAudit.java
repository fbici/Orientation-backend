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
@Table(name = "doc_document_audits")
public class DocumentAudit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false, foreignKey = @ForeignKey(name = "fk_doc_audit_document"))
    private Document document;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(length = 100)
    private String userId;

    @Column(length = 255)
    private String ipAddress;

    @Column(columnDefinition = "text")
    private String details;

    @Column(nullable = false)
    private Instant performedAt;
}
