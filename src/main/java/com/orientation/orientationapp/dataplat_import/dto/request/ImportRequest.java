package com.orientation.orientationapp.dataplat_import.dto.request;

import com.orientation.orientationapp.dataplat_formats.enums.DataFormat;
import com.orientation.orientationapp.dataplat_formats.enums.DataType;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportRequest {

    private UUID campaignId;
    private UUID academicYearId;
    private UUID countryId;
    private DataType dataType;
    private DataFormat format;
    private String source;
    private boolean dryRun;
    private boolean skipValidation;
    private Map<String, String> options;
}
