package com.orientation.orientationapp.knowledge.similarity.service.impl;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.math.BigDecimal;

import com.orientation.orientationapp.knowledge.entity.SimilarityMatrix;
import com.orientation.orientationapp.knowledge.repository.SimilarityMatrixRepository;
import com.orientation.orientationapp.knowledge.similarity.service.SimilarityEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultSimilarityEngineService implements SimilarityEngineService {

    private final SimilarityMatrixRepository similarityMatrixRepository;

    @Override
    public List<Map<String, Object>> findSimilar(String entityType, UUID entityId, int limit) {
        log.info("Finding similar {} for entity {}", entityType, entityId);

        List<SimilarityMatrix> similarities = similarityMatrixRepository
                .findByEntityTypeAndEntityIdA(entityType, entityId);

        return similarities.stream()
                .sorted((a, b) -> b.getSimilarityScore().compareTo(a.getSimilarityScore()))
                .limit(limit)
                .map(s -> Map.<String, Object>of(
                        "entityId", s.getEntityIdB(),
                        "score", s.getSimilarityScore(),
                        "algorithm", s.getAlgorithm()
                ))
                .toList();
    }

    @Override
    public double computeSimilarity(String entityType, UUID idA, UUID idB) {
        return similarityMatrixRepository.findByEntityTypeAndEntityIdA(entityType, idA).stream()
                .filter(s -> s.getEntityIdB().equals(idB))
                .map(s -> s.getSimilarityScore().doubleValue())
                .findFirst()
                .orElse(0.0);
    }
}
