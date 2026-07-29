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
@Table(name = "doc_document_metadata")
public class DocumentMetadata extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_doc_metadata_document"))
    private Document document;

    @Column(length = 500)
    private String title;

    @Column(length = 200)
    private String author;

    @Column(length = 50)
    private String year;

    @Column(length = 50)
    private String docVersion;

    @Column(length = 200)
    private String university;

    @Column(length = 100)
    private String country;

    @Column(length = 10)
    private String language;

    private Integer pageCount;

    private Instant createdDate;

    @Column(columnDefinition = "jsonb")
    private String keywords;

    @Column(length = 500)
    private String checksum;

    @Column(columnDefinition = "jsonb")
    private String customMetadata;
}
