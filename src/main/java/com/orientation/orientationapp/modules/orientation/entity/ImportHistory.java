package com.orientation.orientationapp.modules.orientation.entity;

import com.orientation.orientationapp.common.enums.ImportStatus;
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
@Table(name = "import_histories")
public class ImportHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_version_id", foreignKey = @ForeignKey(name = "fk_import_history_guide_version"))
    private GuideVersion guideVersion;

    @Column(nullable = false, length = 50)
    private String importType;

    @Column(nullable = false, length = 200)
    private String source;

    @Column(length = 255)
    private String fileName;

    @Column(length = 500)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ImportStatus status = ImportStatus.PENDING;

    private Integer totalRecords;

    private Integer processedRecords;

    private Integer successRecords;

    private Integer failedRecords;

    @Column(columnDefinition = "text")
    private String errorLog;

    @Column(length = 255)
    private String importedBy;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant completedAt;

    private Long duration;
}
