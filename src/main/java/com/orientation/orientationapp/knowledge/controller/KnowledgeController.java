package com.orientation.orientationapp.knowledge.controller;

import com.orientation.orientationapp.knowledge.engine.service.KnowledgeEngineService;
import com.orientation.orientationapp.knowledge.explanation.service.ExplainableAiServiceV2;
import com.orientation.orientationapp.knowledge.graph.service.KnowledgeGraphService;
import com.orientation.orientationapp.knowledge.learning.service.RecommendationLearningService;
import com.orientation.orientationapp.knowledge.learning.service.RecommendationLearningService;
import com.orientation.orientationapp.knowledge.search.service.SemanticSearchService;
import com.orientation.orientationapp.knowledge.similarity.service.SimilarityEngineService;
import com.orientation.orientationapp.knowledge.smartquery.service.SmartQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeEngineService knowledgeEngineService;
    private final SemanticSearchService semanticSearchService;
    private final SimilarityEngineService similarityEngineService;
    private final ExplainableAiServiceV2 explainableAiService;
    private final RecommendationLearningService learningService;
    private final KnowledgeGraphService knowledgeGraphService;
    private final SmartQueryService smartQueryService;

    @PostMapping("/rebuild")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> rebuild() {
        knowledgeEngineService.rebuildIndex();
        return ResponseEntity.ok(Map.of("message", "Knowledge index rebuilt successfully"));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(knowledgeEngineService.getStatistics());
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT')")
    public ResponseEntity<?> search(
            @RequestParam String query,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "10") int limit) {
        if (type != null) {
            return ResponseEntity.ok(semanticSearchService.search(query, type, limit));
        }
        return ResponseEntity.ok(semanticSearchService.search(query, limit));
    }

    @GetMapping("/similar")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT')")
    public ResponseEntity<?> getSimilar(
            @RequestParam String entityType,
            @RequestParam UUID entityId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(similarityEngineService.findSimilar(entityType, entityId, limit));
    }

    @PostMapping("/query")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT')")
    public ResponseEntity<Map<String, Object>> query(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(smartQueryService.processQuery(request.get("query")));
    }

    @GetMapping("/graph")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getGraph() {
        return ResponseEntity.ok(knowledgeGraphService.getGraph());
    }

    @GetMapping("/explanations/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT')")
    public ResponseEntity<Map<String, Object>> getExplanation(@PathVariable UUID id) {
        return ResponseEntity.ok(explainableAiService.explain(id));
    }

    @GetMapping("/recommendations/improve")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getImprovementSuggestions(@RequestParam UUID programId) {
        return ResponseEntity.ok(learningService.getImprovementSuggestions(programId));
    }

    @PostMapping("/feedback")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT')")
    public ResponseEntity<Map<String, String>> recordFeedback(@RequestBody Map<String, Object> request) {
        UUID candidateId = UUID.fromString((String) request.get("candidateId"));
        UUID programId = UUID.fromString((String) request.get("programId"));
        String feedbackType = (String) request.get("feedbackType");
        BigDecimal rating = request.get("rating") != null ? new BigDecimal(request.get("rating").toString()) : null;

        learningService.recordFeedback(candidateId, programId,
                com.orientation.orientationapp.knowledge.entity.RecommendationFeedback.FeedbackType.valueOf(feedbackType),
                rating, (String) request.get("comment"));

        return ResponseEntity.ok(Map.of("message", "Feedback recorded successfully"));
    }
}
