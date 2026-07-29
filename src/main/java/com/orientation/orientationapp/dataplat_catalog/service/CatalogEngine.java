package com.orientation.orientationapp.dataplat_catalog.service;

import com.orientation.orientationapp.dataplat_formats.core.model.VersionInfo;
import com.orientation.orientationapp.dataplat_formats.enums.DataType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Engine for the data catalog.
 * Provides a unified view of all available data.
 */
public interface CatalogEngine {

    /**
     * Get all available data types and their versions.
     *
     * @param campaignId the campaign ID
     * @return map of data type to versions
     */
    Map<DataType, List<VersionInfo>> getCatalog(UUID campaignId);

    /**
     * Get the active version for each data type in a campaign.
     *
     * @param campaignId the campaign ID
     * @return map of data type to active version
     */
    Map<DataType, VersionInfo> getActiveVersions(UUID campaignId);

    /**
     * Get data statistics for a campaign.
     *
     * @param campaignId the campaign ID
     * @return statistics map
     */
    Map<String, Object> getStatistics(UUID campaignId);

    /**
     * Search across all data.
     *
     * @param campaignId the campaign ID
     * @param query      the search query
     * @return search results
     */
    List<Map<String, Object>> search(UUID campaignId, String query);
}
