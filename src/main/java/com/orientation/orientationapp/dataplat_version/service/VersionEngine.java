package com.orientation.orientationapp.dataplat_version.service;

import com.orientation.orientationapp.dataplat_formats.core.model.VersionInfo;
import com.orientation.orientationapp.dataplat_formats.enums.DataType;

import java.util.List;
import java.util.UUID;

/**
 * Engine for managing data versions.
 */
public interface VersionEngine {

    /**
     * Create a new version for an import.
     *
     * @param campaignId the campaign ID
     * @param dataType   the data type
     * @param changeDesc description of changes
     * @return the new version info
     */
    VersionInfo createVersion(UUID campaignId, DataType dataType, String changeDesc);

    /**
     * Get the active version for a campaign and data type.
     *
     * @param campaignId the campaign ID
     * @param dataType   the data type
     * @return the active version, or null
     */
    VersionInfo getActiveVersion(UUID campaignId, DataType dataType);

    /**
     * Get all versions for a campaign.
     *
     * @param campaignId the campaign ID
     * @return list of versions
     */
    List<VersionInfo> getVersionHistory(UUID campaignId);

    /**
     * Activate a specific version (deactivate others).
     *
     * @param versionId the version to activate
     */
    void activateVersion(UUID versionId);

    /**
     * Get version by ID.
     *
     * @param versionId the version ID
     * @return the version info
     */
    VersionInfo getVersion(UUID versionId);
}
