package com.orientation.orientationapp.dataplat_import.dto.response;

import com.orientation.orientationapp.dataplat_comparison.strategy.DiffResult;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComparisonResponse {

    private UUID versionId1;
    private UUID versionId2;
    private DiffResult diffResult;
    private String summary;
    private int totalChanges;
}
