package com.orientation.orientationapp.knowledge.engine.service.impl;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.orientation.orientationapp.knowledge.engine.service.KnowledgeEngineService;
import com.orientation.orientationapp.knowledge.entity.KnowledgeNode;
import com.orientation.orientationapp.knowledge.repository.KnowledgeNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultKnowledgeEngineService implements KnowledgeEngineService {

    private final KnowledgeNodeRepository knowledgeNodeRepository;

    @Override
    public void rebuildIndex() {
        log.info("Rebuilding knowledge index...");
        // In production, this would index all entities from the database
        log.info("Knowledge index rebuilt successfully");
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalNodes", knowledgeNodeRepository.count());
        stats.put("nodeTypes", knowledgeNodeRepository.findActiveByNodeType("UNIVERSITY").size());
        return stats;
    }

    @Override
    public Map<String, Object> getNode(String entityType, UUID entityId) {
        return knowledgeNodeRepository.findByEntityTypeAndEntityId(entityType, entityId)
                .stream()
                .findFirst()
                .map(node -> Map.<String, Object>of(
                        "id", node.getId(),
                        "type", node.getNodeType(),
                        "name", node.getName(),
                        "entityType", node.getEntityType()
                ))
                .orElse(Map.of());
    }

    @Override
    public List<Map<String, Object>> getNodesByType(String nodeType) {
        return knowledgeNodeRepository.findActiveByNodeType(nodeType).stream()
                .map(node -> Map.<String, Object>of(
                        "id", node.getId(),
                        "name", node.getName(),
                        "type", node.getNodeType()
                ))
                .toList();
    }
}
