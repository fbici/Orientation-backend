package com.orientation.orientationapp.dataplat_import.service;

import com.orientation.orientationapp.dataplat_formats.core.model.ImportContext;
import com.orientation.orientationapp.dataplat_formats.core.model.ImportResult;

/**
 * Main orchestrator for the import pipeline.
 * Coordinates parsing, validation, transformation, and persistence.
 */
public interface ImportOrchestrator {

    /**
     * Execute the full import pipeline.
     *
     * @param context the import context
     * @return the import result
     */
    ImportResult executeImport(ImportContext context);

    /**
     * Execute a dry-run import (validation only, no persistence).
     *
     * @param context the import context
     * @return the import result with validation issues
     */
    ImportResult executeDryRun(ImportContext context);

    /**
     * Cancel a running import.
     *
     * @param importId the import ID
     */
    void cancelImport(java.util.UUID importId);

    /**
     * Get the status of a running import.
     *
     * @param importId the import ID
     * @return the current import result
     */
    ImportResult getImportStatus(java.util.UUID importId);
}
