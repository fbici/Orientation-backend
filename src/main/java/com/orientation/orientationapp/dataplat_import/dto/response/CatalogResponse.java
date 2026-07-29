package com.orientation.orientationapp.dataplat_import.dto.response;

import com.orientation.orientationapp.dataplat_formats.core.model.VersionInfo;
import com.orientation.orientationapp.dataplat_formats.enums.DataType;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogResponse {

    private UUID campaignId;
    private Map<DataType, List<VersionInfo>> catalog;
    private Map<DataType, VersionInfo> activeVersions;
    private Map<String, Object> statistics;
}
