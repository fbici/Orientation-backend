package com.orientation.orientationapp.knowledge.graph.service.impl;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.orientation.orientationapp.knowledge.entity.KnowledgeNode;
import com.orientation.orientationapp.knowledge.entity.KnowledgeRelation;
import com.orientation.orientationapp.knowledge.graph.service.KnowledgeGraphService;
import com.orientation.orientationapp.knowledge.repository.KnowledgeNodeRepository;
import com.orientation.orientationapp.knowledge.repository.KnowledgeRelationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultKnowledgeGraphService implements KnowledgeGraphService {

    private final KnowledgeNodeRepository knowledgeNodeRepository;
    private final KnowledgeRelationRepository knowledgeRelationRepository;

    @Override
    public Map<String, Object> getGraph() {
        List<KnowledgeNode> nodes = knowledgeNodeRepository.findAll();
        List<KnowledgeRelation> relations = knowledgeRelationRepository.findAll();

        List<Map<String, Object>> nodeList = nodes.stream()
                .map(n -> Map.<String, Object>of("id", n.getId(), "label", n.getName(), "type", n.getNodeType()))
                .toList();

        List<Map<String, Object>> edgeList = relations.stream()
                .map(r -> Map.<String, Object>of(
                        "source", r.getSource().getId(),
                        "target", r.getTarget().getId(),
                        "label", r.getRelationType()
                ))
                .toList();

        return Map.of("nodes", nodeList, "edges", edgeList);
    }

    @Override
    public List<Map<String, Object>> getRelatedEntities(String entityType, UUID entityId, int depth) {
        List<KnowledgeNode> related = knowledgeNodeRepository.findByEntityTypeAndEntityId(entityType, entityId);

        return related.stream()
                .flatMap(node -> node.getRelatedNodes().stream())
                .limit(depth * 10)
                .map(n -> Map.<String, Object>of("id", n.getId(), "name", n.getName(), "type", n.getNodeType()))
                .distinct()
                .toList();
    }

    @Override
    public List<Map<String, Object>> findPath(String fromType, UUID fromId, String toType, UUID toId) {
        // Simplified path finding - in production would use BFS/DFS
        List<KnowledgeNode> startNodes = knowledgeNodeRepository.findByEntityTypeAndEntityId(fromType, fromId);
        List<KnowledgeNode> endNodes = knowledgeNodeRepository.findByEntityTypeAndEntityId(toType, toId);

        List<Map<String, Object>> path = new ArrayList<>();
        for (KnowledgeNode start : startNodes) {
            for (KnowledgeNode end : endNodes) {
                path.add(Map.<String, Object>of("from", start.getName(), "to", end.getName()));
            }
        }

        return path;
    }
}
