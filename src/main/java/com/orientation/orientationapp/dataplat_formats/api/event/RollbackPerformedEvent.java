package com.orientation.orientationapp.dataplat_formats.api.event;

import com.orientation.orientationapp.dataplat_formats.core.model.VersionInfo;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RollbackPerformedEvent {

    private UUID sourceVersionId;
    private VersionInfo restoredVersion;
    private String reason;
    private String performedBy;
}
