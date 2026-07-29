package com.orientation.orientationapp.dataplat_comparison.service;

import com.orientation.orientationapp.dataplat_comparison.strategy.DiffResult;
import com.orientation.orientationapp.dataplat_formats.enums.DataType;

import java.util.UUID;

/**
 * Engine for comparing versions of data.
 */
public interface ComparisonEngine {

    /**
     * Compare two versions.
     *
     * @param versionId1 the first version
     * @param versionId2 the second version
     * @param dataType   the data type
     * @return the diff result
     */
    DiffResult compare(UUID versionId1, UUID versionId2, DataType dataType);

    /**
     * Compare a version with the current active version.
     *
     * @param versionId the version to compare
     * @param dataType  the data type
     * @return the diff result
     */
    DiffResult compareWithActive(UUID versionId, DataType dataType);

    /**
     * Get a summary of changes between two versions.
     *
     * @param versionId1 the first version
     * @param versionId2 the second version
     * @param dataType   the data type
     * @return human-readable summary
     */
    String getChangeSummary(UUID versionId1, UUID versionId2, DataType dataType);
}
