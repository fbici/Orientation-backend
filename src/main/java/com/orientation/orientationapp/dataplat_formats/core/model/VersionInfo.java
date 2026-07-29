package com.orientation.orientationapp.dataplat_formats.core.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VersionInfo {

    private UUID versionId;
    private int versionNumber;
    private String versionLabel;
    private UUID campaignId;
    private UUID academicYearId;
    private boolean active;
    private Instant createdAt;
    private UUID createdBy;
    private String changeDescription;

    public static VersionInfo of(UUID versionId, int versionNumber, UUID campaignId) {
        return VersionInfo.builder()
                .versionId(versionId)
                .versionNumber(versionNumber)
                .campaignId(campaignId)
                .active(true)
                .createdAt(Instant.now())
                .build();
    }
}
