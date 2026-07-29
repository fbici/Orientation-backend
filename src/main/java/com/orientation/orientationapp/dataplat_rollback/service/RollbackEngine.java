package com.orientation.orientationapp.dataplat_rollback.service;

import com.orientation.orientationapp.dataplat_formats.core.model.VersionInfo;

import java.util.UUID;

/**
 * Engine for rolling back to previous versions.
 */
public interface RollbackEngine {

    /**
     * Rollback to a specific version.
     *
     * @param targetVersionId the version to restore
     * @param reason          reason for rollback
     * @return the new version created by rollback
     */
    VersionInfo rollback(UUID targetVersionId, String reason);

    /**
     * Rollback the last import for a campaign.
     *
     * @param campaignId the campaign ID
     * @param reason     reason for rollback
     * @return the new version created by rollback
     */
    VersionInfo rollbackLast(UUID campaignId, String reason);

    /**
     * Check if rollback is possible for a version.
     *
     * @param versionId the version to check
     * @return true if rollback is possible
     */
    boolean canRollback(UUID versionId);

    /**
     * Get available rollback targets for a campaign.
     *
     * @param campaignId the campaign ID
     * @return list of versions that can be restored
     */
    java.util.List<VersionInfo> getRollbackTargets(UUID campaignId);
}
