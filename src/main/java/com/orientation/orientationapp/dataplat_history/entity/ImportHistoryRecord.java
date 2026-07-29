package com.orientation.orientationapp.dataplat_history.entity;

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
@Table(name = "import_history_records")
public class ImportHistoryRecord extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 50)
    private String fileType;

    @Column(length = 500)
    private String checksum;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private Integer totalRows;

    @Column(nullable = false)
    @Builder.Default
    private Integer importedRows = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer rejectedRows = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer skippedRows = 0;

    @Column(length = 100)
    private String userId;

    @Column(length = 50)
    private String tenantId;

    @Column(length = 50)
    private String organizationId;

    @Column(nullable = false, length = 50)
    private String dataType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ImportStatus status = ImportStatus.PENDING;

    @Column(length = 1000)
    private String comments;

    @Column(nullable = false)
    private Long executionTimeMs;

    @Column(nullable = false)
    @Builder.Default
    private Integer versionNumber = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_version_id", foreignKey = @ForeignKey(name = "fk_import_history_parent"))
    private ImportHistoryRecord parentVersion;

    public enum ImportStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, ROLLED_BACK
    }
}
