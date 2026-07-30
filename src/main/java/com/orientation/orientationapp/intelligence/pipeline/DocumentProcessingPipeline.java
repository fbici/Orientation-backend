package com.orientation.orientationapp.intelligence.pipeline;

import com.orientation.orientationapp.intelligence.ocr.OcrService;
import com.orientation.orientationapp.intelligence.extraction.EntityExtractionService;
import com.orientation.orientationapp.intelligence.classification.DocumentClassificationService;
import com.orientation.orientationapp.intelligence.knowledge.KnowledgeGraphService;
import com.orientation.orientationapp.intelligence.indexing.SearchIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Pipeline complet de traitement documentaire.
 *
 * Guide PDF → OCR → Extraction → Classification → Knowledge Graph → Indexation
 *
 * Ce service orchestre toute la chaîne d'intelligence documentaire.
 * Chaque étape publie un événement pour le suivi en temps réel.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessingPipeline {

    private final OcrService ocrService;
    private final EntityExtractionService extractionService;
    private final DocumentClassificationService classificationService;
    private final KnowledgeGraphService knowledgeGraphService;
    private final SearchIndexService searchIndexService;
    private final PersistenceService persistenceService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Traite un document complet à travers tout le pipeline.
     *
     * @param documentId ID du document à traiter
     * @param fileContent contenu binaire du fichier
     * @param fileName nom du fichier original
     * @return résultat du traitement avec toutes les entités extraites
     */
    @Transactional
    public PipelineResult processDocument(UUID documentId, byte[] fileContent, String fileName) {
        log.info("=== PIPELINE START: document={} file={} ===", documentId, fileName);
        PipelineResult result = PipelineResult.builder()
                .documentId(documentId)
                .fileName(fileName)
                .startTime(System.currentTimeMillis())
                .build();

        try {
            // === ETAPE 1: OCR ===
            publishProgress(documentId, "OCR", "Demarrage de l'analyse OCR...");
            String extractedText = ocrService.extractText(fileContent, fileName);
            result.setExtractedText(extractedText);
            result.addStep("OCR", extractedText.length() + " caracteres extraits");
            publishProgress(documentId, "OCR", "OCR termine: " + extractedText.length() + " caracteres");

            if (extractedText == null || extractedText.isBlank()) {
                result.setStatus("ERROR");
                result.setError("Aucun texte extrait du document");
                return result;
            }

            // === ETAPE 2: CLASSIFICATION ===
            publishProgress(documentId, "CLASSIFICATION", "Classification du document...");
            String documentType = classificationService.classify(extractedText, fileName);
            result.setDocumentType(documentType);
            result.addStep("CLASSIFICATION", "Type: " + documentType);
            publishProgress(documentId, "CLASSIFICATION", "Classifie comme: " + documentType);

            // === ETAPE 3: EXTRACTION D'ENTITES ===
            publishProgress(documentId, "EXTRACTION", "Extraction des entites...");
            EntityExtractionService.ExtractedEntities entities = extractionService.extract(extractedText, documentType);
            result.setEntities(entities);
            result.addStep("EXTRACTION", buildExtractionSummary(entities));
            publishProgress(documentId, "EXTRACTION", buildExtractionSummary(entities));

            // === ETAPE 4: INSERTION EN BASE ===
            publishProgress(documentId, "PERSISTENCE", "Insertion dans PostgreSQL...");
            persistenceService.persist(entities, documentId);
            result.addStep("PERSISTENCE", "Entites persistees en base");
            publishProgress(documentId, "PERSISTENCE", "Donnees sauvegardees");

            // === ETAPE 5: KNOWLEDGE GRAPH ===
            publishProgress(documentId, "KNOWLEDGE_GRAPH", "Mise a jour du Knowledge Graph...");
            knowledgeGraphService.updateGraph(entities, documentId);
            result.addStep("KNOWLEDGE_GRAPH", "Graph mis a jour");
            publishProgress(documentId, "KNOWLEDGE_GRAPH", "Knowledge Graph mis a jour");

            // === ETAPE 6: INDEXATION ===
            publishProgress(documentId, "INDEXING", "Reindexation de la recherche...");
            searchIndexService.reindex(entities, documentId);
            result.addStep("INDEXING", "Index reconstruit");
            publishProgress(documentId, "INDEXING", "Indexation terminee");

            // === ETAPE 7: RECALCUL DES RECOMMANDATIONS ===
            publishProgress(documentId, "RECALC", "Mise a jour des recommandations...");
            // Le recalcul se fait de manière asynchrone via événement
            eventPublisher.publishEvent(new PipelineCompletedEvent(documentId, entities));
            result.addStep("RECALC", "Evenement de recalcul publie");

            result.setStatus("COMPLETED");
            result.setEndTime(System.currentTimeMillis());
            log.info("=== PIPELINE END: document={} status={} duration={}ms ===",
                    documentId, result.getStatus(), result.getEndTime() - result.getStartTime());

        } catch (Exception e) {
            log.error("Pipeline error for document {}: {}", documentId, e.getMessage(), e);
            result.setStatus("ERROR");
            result.setError(e.getMessage());
            result.setEndTime(System.currentTimeMillis());
            publishProgress(documentId, "ERROR", "Erreur: " + e.getMessage());
        }

        return result;
    }

    private void publishProgress(UUID documentId, String step, String message) {
        eventPublisher.publishEvent(new PipelineProgressEvent(documentId, step, message));
    }

    private String buildExtractionSummary(EntityExtractionService.ExtractedEntities entities) {
        List<String> parts = new ArrayList<>();
        if (entities.getUniversities() != null && !entities.getUniversities().isEmpty())
            parts.add(entities.getUniversities().size() + " universites");
        if (entities.getPrograms() != null && !entities.getPrograms().isEmpty())
            parts.add(entities.getPrograms().size() + " programmes");
        if (entities.getCriteria() != null && !entities.getCriteria().isEmpty())
            parts.add(entities.getCriteria().size() + " criteres");
        if (entities.getScholarships() != null && !entities.getScholarships().isEmpty())
            parts.add(entities.getScholarships().size() + " bourses");
        if (entities.getSubjects() != null && !entities.getSubjects().isEmpty())
            parts.add(entities.getSubjects().size() + " matieres");
        return String.join(", ", parts);
    }

    // --- Inner classes for events and results ---

    @lombok.Data
    @lombok.Builder
    public static class PipelineResult {
        private UUID documentId;
        private String fileName;
        private String status;
        private String error;
        private String extractedText;
        private String documentType;
        private EntityExtractionService.ExtractedEntities entities;
        private List<String[]> steps = new ArrayList<>();
        private long startTime;
        private long endTime;

        public void addStep(String name, String detail) {
            steps.add(new String[]{name, detail});
        }
    }

    @lombok.AllArgsConstructor
    @lombok.Getter
    public static class PipelineProgressEvent {
        private final UUID documentId;
        private final String step;
        private final String message;
    }

    @lombok.AllArgsConstructor
    @lombok.Getter
    public static class PipelineCompletedEvent {
        private final UUID documentId;
        private final EntityExtractionService.ExtractedEntities entities;
    }
}
