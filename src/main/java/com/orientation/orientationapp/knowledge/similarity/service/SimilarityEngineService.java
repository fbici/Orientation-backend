package com.orientation.orientationapp.knowledge.similarity.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface SimilarityEngineService {

    /**
     * Find similar entities.
     *
     * @param entityType the entity type
     * @param entityId   the entity ID
     * @param limit      max results
     * @return similar entities with scores
     */
    List<Map<String, Object>> findSimilar(String entityType, UUID entityId, int limit);

    /**
     * Compute similarity between two entities.
     *
     * @param entityType entity type
     * @param idA        first entity
     * @param idB        second entity
     * @return similarity score
     */
    double computeSimilarity(String entityType, UUID idA, UUID idB);
}
