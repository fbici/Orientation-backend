package com.orientation.orientationapp.dataplat_history.service;

import com.orientation.orientationapp.dataplat_formats.core.model.ImportMetadata;
import com.orientation.orientationapp.dataplat_formats.core.model.ImportResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Engine for tracking import history and audit trail.
 */
public interface HistoryEngine {

    /**
     * Record an import event.
     *
     * @param metadata the import metadata
     * @param result   the import result
     */
    void recordImport(ImportMetadata metadata, ImportResult result);

    /**
     * Get import history for a campaign.
     *
     * @param campaignId the campaign ID
     * @return list of import metadata
     */
    List<ImportMetadata> getHistory(UUID campaignId);

    /**
     * Get import history within a date range.
     *
     * @param from start date
     * @param to   end date
     * @return list of import metadata
     */
    List<ImportMetadata> getHistory(Instant from, Instant to);

    /**
     * Get a specific import by ID.
     *
     * @param importId the import ID
     * @return the import metadata
     */
    ImportMetadata getImport(UUID importId);

    /**
     * Get imports by user.
     *
     * @param userId the user ID
     * @return list of imports
     */
    List<ImportMetadata> getImportsByUser(String userId);
}
