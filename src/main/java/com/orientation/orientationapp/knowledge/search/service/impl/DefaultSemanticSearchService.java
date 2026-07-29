package com.orientation.orientationapp.knowledge.search.service.impl;
import java.util.List;
import java.util.Map;

import com.orientation.orientationapp.knowledge.search.service.SemanticSearchService;
import com.orientation.orientationapp.knowledge.entity.SearchIndex;
import com.orientation.orientationapp.knowledge.repository.SearchIndexRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultSemanticSearchService implements SemanticSearchService {

    private final SearchIndexRepository searchIndexRepository;

    @Override
    public List<Map<String, Object>> search(String query, int limit) {
        log.info("Semantic search: {} (limit: {})", query, limit);

        List<SearchIndex> results = searchIndexRepository.search(query);

        return results.stream()
                .limit(limit)
                .map(index -> Map.<String, Object>of(
                        "id", index.getEntityId(),
                        "type", index.getEntityType(),
                        "name", index.getName(),
                        "content", index.getContent() != null ? index.getContent().substring(0, Math.min(200, index.getContent().length())) : ""
                ))
                .toList();
    }

    @Override
    public List<Map<String, Object>> search(String query, String entityType, int limit) {
        log.info("Semantic search: {} in {} (limit: {})", query, entityType, limit);

        List<SearchIndex> results = searchIndexRepository.findByEntityType(entityType).stream()
                .filter(index -> index.getName().toLowerCase().contains(query.toLowerCase()) ||
                                 (index.getContent() != null && index.getContent().toLowerCase().contains(query.toLowerCase())))
                .toList();

        return results.stream()
                .limit(limit)
                .map(index -> Map.<String, Object>of(
                        "id", index.getEntityId(),
                        "type", index.getEntityType(),
                        "name", index.getName()
                ))
                .toList();
    }
}
