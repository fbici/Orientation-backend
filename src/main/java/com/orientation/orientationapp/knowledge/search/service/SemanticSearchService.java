package com.orientation.orientationapp.knowledge.search.service;

import java.util.List;
import java.util.Map;

public interface SemanticSearchService {

    /**
     * Search the knowledge base semantically.
     *
     * @param query the search query
     * @param limit max results
     * @return search results
     */
    List<Map<String, Object>> search(String query, int limit);

    /**
     * Search with filters.
     *
     * @param query     the search query
     * @param entityType entity type filter
     * @param limit     max results
     * @return search results
     */
    List<Map<String, Object>> search(String query, String entityType, int limit);
}
