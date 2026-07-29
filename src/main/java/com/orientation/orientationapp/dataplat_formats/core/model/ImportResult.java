package com.orientation.orientationapp.dataplat_formats.core.model;

import com.orientation.orientationapp.dataplat_formats.enums.ImportStatus;
import lombok.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportResult {

    private UUID importId;
    private ImportStatus status;
    private int totalRecords;
    private int processedRecords;
    private int successRecords;
    private int failedRecords;
    private int skippedRecords;
    private Instant startedAt;
    private Instant completedAt;
    private Duration duration;

    @Builder.Default
    private List<ValidationIssue> issues = new ArrayList<>();

    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    @Builder.Default
    private List<String> errors = new ArrayList<>();

    private QualityReport qualityReport;
    private VersionInfo versionInfo;

    public void addIssue(ValidationIssue issue) {
        this.issues.add(issue);
        if (issue.getSeverity().isError()) {
            this.failedRecords++;
        }
    }

    public void markCompleted() {
        this.status = ImportStatus.COMPLETED;
        this.completedAt = Instant.now();
        if (startedAt != null) {
            this.duration = Duration.between(startedAt, completedAt);
        }
    }

    public void markFailed(String error) {
        this.status = ImportStatus.FAILED;
        this.errors.add(error);
        this.completedAt = Instant.now();
        if (startedAt != null) {
            this.duration = Duration.between(startedAt, completedAt);
        }
    }
}
