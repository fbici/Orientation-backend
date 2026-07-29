package com.orientation.orientationapp.dataplat_formats.api.event;

import com.orientation.orientationapp.dataplat_formats.core.model.VersionInfo;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VersionCreatedEvent {

    private VersionInfo versionInfo;
    private String createdBy;
}
