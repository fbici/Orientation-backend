package com.orientation.orientationapp.dataplat_formats.core.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportMetadata {

    private UUID importId;
    private String fileName;
    private String originalFileName;
    private long fileSize;
    private String mimeType;
    private String sha256Hash;
    private String source;
    private UUID versionId;
    private String uploadedBy;
    private Instant uploadedAt;
    private Instant completedAt;
    private long durationMs;
    private int totalRecords;
    private int successRecords;
    private int failedRecords;
    private int warningCount;
    private int errorCount;
    private boolean integrityVerified;
    private String checksumVerified;
}
