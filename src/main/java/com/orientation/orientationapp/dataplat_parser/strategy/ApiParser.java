package com.orientation.orientationapp.dataplat_parser.strategy;

import com.orientation.orientationapp.dataplat_formats.core.model.ImportContext;

import java.util.List;
import java.util.Map;

/**
 * Strategy interface for parsing data from external APIs.
 */
public interface ApiParser {

    /**
     * Fetch and parse data from an external API.
     *
     * @param apiEndpoint the API URL
     * @param context     the import context
     * @return list of rows parsed from API response
     */
    List<Map<String, Object>> fetchAndParse(String apiEndpoint, ImportContext context);

    /**
     * Validate API connectivity and response format.
     *
     * @param apiEndpoint the API URL
     * @return true if the API is accessible and returns valid data
     */
    boolean validateApi(String apiEndpoint);

    /**
     * @return the API identifier
     */
    String getApiIdentifier();
}
