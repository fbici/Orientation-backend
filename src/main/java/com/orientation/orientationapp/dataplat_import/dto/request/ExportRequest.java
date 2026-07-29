package com.orientation.orientationapp.dataplat_import.dto.request;

import com.orientation.orientationapp.dataplat_formats.enums.DataFormat;
import com.orientation.orientationapp.dataplat_formats.enums.DataType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportRequest {

    private UUID campaignId;
    private UUID versionId;
    private DataType dataType;
    private DataFormat format;
}
