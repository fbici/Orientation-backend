package com.orientation.orientationapp.dataplat_import.dto.response;

import com.orientation.orientationapp.dataplat_formats.core.model.ImportResult;
import com.orientation.orientationapp.dataplat_formats.enums.ImportStatus;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportResponse {

    private UUID importId;
    private ImportStatus status;
    private int totalRecords;
    private int successRecords;
    private int failedRecords;
    private String message;

    public static ImportResponse fromResult(ImportResult result) {
        return ImportResponse.builder()
                .importId(result.getImportId())
                .status(result.getStatus())
                .totalRecords(result.getTotalRecords())
                .successRecords(result.getSuccessRecords())
                .failedRecords(result.getFailedRecords())
                .message(result.getStatus().getDescription())
                .build();
    }
}
