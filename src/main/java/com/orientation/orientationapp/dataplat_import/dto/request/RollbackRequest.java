package com.orientation.orientationapp.dataplat_import.dto.request;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RollbackRequest {

    private UUID targetVersionId;
    private UUID campaignId;
    private String reason;
}
