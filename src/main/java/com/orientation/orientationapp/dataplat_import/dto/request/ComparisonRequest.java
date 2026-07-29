package com.orientation.orientationapp.dataplat_import.dto.request;

import com.orientation.orientationapp.dataplat_formats.enums.DataType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComparisonRequest {

    private UUID versionId1;
    private UUID versionId2;
    private DataType dataType;
    private UUID campaignId;
}
