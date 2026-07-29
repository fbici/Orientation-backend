package com.orientation.orientationapp.knowledge.smartquery.service;

import java.util.Map;

public interface SmartQueryService {

    /**
     * Process a natural language query.
     *
     * @param query the natural language query
     * @return structured results
     */
    Map<String, Object> processQuery(String query);

    /**
     * Get query suggestions.
     *
     * @param partial partial query
     * @return suggestions
     */
    Map<String, Object> getSuggestions(String partial);
}
