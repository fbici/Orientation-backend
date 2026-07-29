package com.orientation.orientationapp.knowledge.graph.service;
import java.util.UUID;

import java.util.List;
import java.util.Map;

public interface KnowledgeGraphService {

    /**
     * Get the full knowledge graph.
     *
     * @return graph with nodes and edges
     */
    Map<String, Object> getGraph();

    /**
     * Get graph for a specific entity.
     *
     * @param entityType the entity type
     * @param entityId   the entity ID
     * @param depth      traversal depth
     * @return related entities
     */
    List<Map<String, Object>> getRelatedEntities(String entityType, UUID entityId, int depth);

    /**
     * Find paths between two entities.
     *
     * @param fromType source type
     * @param fromId   source ID
     * @param toType   target type
     * @param toId     target ID
     * @return path
     */
    List<Map<String, Object>> findPath(String fromType, UUID fromId, String toType, UUID toId);
}
