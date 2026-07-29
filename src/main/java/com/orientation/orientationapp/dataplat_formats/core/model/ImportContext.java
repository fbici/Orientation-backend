package com.orientation.orientationapp.dataplat_formats.core.model;

import com.orientation.orientationapp.dataplat_formats.enums.DataFormat;
import com.orientation.orientationapp.dataplat_formats.enums.DataType;
import lombok.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportContext {

    private UUID importId;
    private UUID campaignId;
    private UUID academicYearId;
    private UUID countryId;
    private DataType dataType;
    private DataFormat sourceFormat;
    private String sourceIdentifier;
    private String uploadedBy;
    private Instant startedAt;
    private Map<String, Object> properties;

    @Builder.Default
    private boolean dryRun = false;

    @Builder.Default
    private boolean skipValidation = false;

    public static ImportContext create(UUID campaignId, UUID academicYearId, DataType dataType, DataFormat format) {
        return ImportContext.builder()
                .importId(UUID.randomUUID())
                .campaignId(campaignId)
                .academicYearId(academicYearId)
                .dataType(dataType)
                .sourceFormat(format)
                .startedAt(Instant.now())
                .properties(new HashMap<>())
                .build();
    }
}
