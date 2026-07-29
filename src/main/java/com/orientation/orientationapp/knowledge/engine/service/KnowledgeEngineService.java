package com.orientation.orientationapp.knowledge.engine.service;
import java.util.List;
import java.util.UUID;

import java.util.Map;

public interface KnowledgeEngineService {

    /**
     * Rebuild the entire knowledge index from database.
     */
    void rebuildIndex();

    /**
     * Get knowledge statistics.
     *
     * @return statistics map
     */
    Map<String, Object> getStatistics();

    /**
     * Get a knowledge node by entity.
     *
     * @param entityType the entity type
     * @param entityId   the entity ID
     * @return the node properties
     */
    Map<String, Object> getNode(String entityType, UUID entityId);

    /**
     * Get all nodes of a specific type.
     *
     * @param nodeType the node type
     * @return list of nodes
     */
    List<Map<String, Object>> getNodesByType(String nodeType);
}
