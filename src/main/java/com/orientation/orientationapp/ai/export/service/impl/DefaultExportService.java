package com.orientation.orientationapp.ai.export.service.impl;

import com.orientation.orientationapp.ai.export.model.ExportResult;
import com.orientation.orientationapp.ai.export.service.ExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
public class DefaultExportService implements ExportService {

    @Override
    public ExportResult exportToPdf(UUID recommendationId) {
        log.info("Exporting recommendation {} to PDF", recommendationId);

        // Placeholder for PDF generation
        String content = "Recommendation Report - " + recommendationId;

        return ExportResult.builder()
                .format("PDF")
                .data(content.getBytes(StandardCharsets.UTF_8))
                .fileName("recommendation_" + recommendationId + ".pdf")
                .fileSize(content.getBytes().length)
                .mimeType("application/pdf")
                .build();
    }

    @Override
    public ExportResult exportToExcel(UUID recommendationId) {
        log.info("Exporting recommendation {} to Excel", recommendationId);

        String content = "Recommendation ID,Score,Program,University\n" + recommendationId + ",85,Informatique,UM5";

        return ExportResult.builder()
                .format("EXCEL")
                .data(content.getBytes(StandardCharsets.UTF_8))
                .fileName("recommendation_" + recommendationId + ".xlsx")
                .fileSize(content.getBytes().length)
                .mimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .build();
    }

    @Override
    public ExportResult exportToCsv(UUID recommendationId) {
        log.info("Exporting recommendation {} to CSV", recommendationId);

        String content = "Recommendation ID,Score,Program,University\n" + recommendationId + ",85,Informatique,UM5";

        return ExportResult.builder()
                .format("CSV")
                .data(content.getBytes(StandardCharsets.UTF_8))
                .fileName("recommendation_" + recommendationId + ".csv")
                .fileSize(content.getBytes().length)
                .mimeType("text/csv")
                .build();
    }
}
