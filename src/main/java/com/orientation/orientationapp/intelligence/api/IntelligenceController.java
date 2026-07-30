package com.orientation.orientationapp.intelligence.api;

import com.orientation.orientationapp.intelligence.knowledge.KnowledgeGraphService;
import com.orientation.orientationapp.intelligence.learning.LearningEngine;
import com.orientation.orientationapp.intelligence.pipeline.DocumentProcessingPipeline;
import com.orientation.orientationapp.intelligence.smartquery.SmartQueryEngine;
import com.orientation.orientationapp.modules.knowledge.entity.KnowledgeNode;
import jakarta.validation.Valid;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * API REST pour le module Intelligence.
 *
 * Endpoints :
 * - POST /intelligence/process : Traiter un document (pipeline complet)
 * - POST /intelligence/smart-query : Requête en langage naturel
 * - GET  /intelligence/knowledge : Rechercher dans le Knowledge Graph
 * - POST /intelligence/feedback : Enregistrer un feedback
 * - GET  /intelligence/history : Historique d'apprentissage
 */
@Slf4j
@RestController
@RequestMapping("/intelligence")
@RequiredArgsConstructor
public class IntelligenceController {

    private final DocumentProcessingPipeline pipeline;
    private final SmartQueryEngine smartQueryEngine;
    private final KnowledgeGraphService knowledgeGraphService;
    private final LearningEngine learningEngine;

    /**
     * Traite un document à travers le pipeline complet.
     *
     * Upload → OCR → Extraction → Classification → Knowledge Graph → Indexation
     */
    @PostMapping("/process")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<DocumentProcessingPipeline.PipelineResult> processDocument(
            @RequestParam("file") MultipartFile file) {
        try {
            log.info("Processing document: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
            DocumentProcessingPipeline.PipelineResult result = pipeline.processDocument(
                    UUID.randomUUID(), file.getBytes(), file.getOriginalFilename());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Document processing failed", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Requête en langage naturel.
     *
     * Exemples :
     * - "Je veux étudier l'IA au Canada"
     * - "Quelles universités acceptent un Bac D ?"
     * - "Je cherche une bourse en France"
     */
    @PostMapping("/smart-query")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT')")
    public ResponseEntity<SmartQueryEngine.SmartQueryResult> smartQuery(@Valid @RequestBody SmartQueryRequest request) {
        log.info("Smart Query: {}", request.getQuery());
        SmartQueryEngine.SmartQueryResult result = smartQueryEngine.query(request.getQuery());
        return ResponseEntity.ok(result);
    }

    /**
     * Recherche dans le Knowledge Graph.
     */
    @GetMapping("/knowledge")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT')")
    public ResponseEntity<List<KnowledgeNode>> searchKnowledge(@RequestParam String q) {
        List<KnowledgeNode> nodes = knowledgeGraphService.search(q);
        return ResponseEntity.ok(nodes);
    }

    /**
     * Récupère les nœuds liés à un nœud donné.
     */
    @GetMapping("/knowledge/{id}/related")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT')")
    public ResponseEntity<List<KnowledgeNode>> getRelatedNodes(@PathVariable UUID id) {
        List<KnowledgeNode> nodes = knowledgeGraphService.getRelatedNodes(id);
        return ResponseEntity.ok(nodes);
    }

    /**
     * Enregistre un feedback (acceptation/refus d'une recommandation).
     */
    @PostMapping("/feedback")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT')")
    public ResponseEntity<Void> recordFeedback(@Valid @RequestBody FeedbackRequest request) {
        log.info("Feedback: recommendation={} action={}", request.getRecommendationId(), request.getAction());

        if ("ACCEPTED".equals(request.getAction())) {
            learningEngine.recordAcceptance(request.getRecommendationId(), request.getCandidateId(), request.getProgramId());
        } else if ("REJECTED".equals(request.getAction())) {
            learningEngine.recordRejection(request.getRecommendationId(), request.getCandidateId(), request.getProgramId(), request.getReason());
        } else {
            learningEngine.recordView(request.getRecommendationId(), request.getCandidateId(), request.getProgramId());
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Récupère l'historique d'apprentissage d'un candidat.
     */
    @GetMapping("/history/{candidateId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT')")
    public ResponseEntity<?> getLearningHistory(@PathVariable UUID candidateId) {
        return ResponseEntity.ok(learningEngine.getHistory(candidateId));
    }

    // --- DTOs ---

    @Data
    public static class SmartQueryRequest {
        @jakarta.validation.constraints.NotBlank
        private String query;
    }

    @Data
    public static class FeedbackRequest {
        private UUID recommendationId;
        private UUID candidateId;
        private UUID programId;
        private String action; // ACCEPTED, REJECTED, VIEWED
        private String reason;
    }
}
