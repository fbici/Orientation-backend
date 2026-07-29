package com.orientation.orientationapp.ai.export.service;

import java.util.UUID;

import com.orientation.orientationapp.ai.export.model.ExportResult;

public interface ExportService {

    /**
     * Export recommendations to PDF.
     *
     * @param recommendationId the recommendation ID
     * @return the export result
     */
    ExportResult exportToPdf(UUID recommendationId);

    /**
     * Export recommendations to Excel.
     *
     * @param recommendationId the recommendation ID
     * @return the export result
     */
    ExportResult exportToExcel(UUID recommendationId);

    /**
     * Export recommendations to CSV.
     *
     * @param recommendationId the recommendation ID
     * @return the export result
     */
    ExportResult exportToCsv(UUID recommendationId);
}
