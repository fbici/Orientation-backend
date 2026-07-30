package com.orientation.orientationapp.intelligence.classification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Classificateur de documents basé sur des règles.
 *
 * Analyse le contenu textuel et le nom de fichier pour déterminer
 * le type de document (guide, règlement, brochure, etc.)
 */
@Slf4j
@Component
public class RuleBasedDocumentClassifier {

    /**
     * Classifie un document basé sur son contenu et son nom.
     */
    public String classify(String text, String fileName) {
        String lower = text.toLowerCase();
        String fileLower = fileName.toLowerCase();

        Map<String, Integer> scores = new HashMap<>();

        // Règles de classification
        scores.put("GUIDE", calculateGuideScore(lower, fileLower));
        scores.put("REGLEMENT", calculateReglementScore(lower));
        scores.put("BROCHURE", calculateBrochureScore(lower));
        scores.put("CATALOGUE", calculateCatalogueScore(lower));
        scores.put("BOURSE", calculateBourseScore(lower, fileLower));
        scores.put("TRANSCRIPT", calculateTranscriptScore(lower, fileLower));

        // Retourner le type avec le score le plus élevé
        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .filter(e -> e.getValue() > 0)
                .map(Map.Entry::getKey)
                .orElse("OTHER");
    }

    private int calculateGuideScore(String text, String fileName) {
        int score = 0;
        if (text.contains("guide")) score += 5;
        if (fileName.contains("guide")) score += 5;
        if (text.contains("orientation")) score += 3;
        if (text.contains("admission")) score += 2;
        if (text.contains("inscription")) score += 2;
        if (text.contains("candidature")) score += 2;
        if (text.contains("rentrée")) score += 2;
        return score;
    }

    private int calculateReglementScore(String text) {
        int score = 0;
        if (text.contains("règlement")) score += 5;
        if (text.contains("article")) score += 3;
        if (text.contains("conditions d'admission")) score += 4;
        if (text.contains("critères de sélection")) score += 4;
        return score;
    }

    private int calculateBrochureScore(String text) {
        int score = 0;
        if (text.contains("brochure")) score += 5;
        if (text.contains("présentation")) score += 3;
        if (text.contains("campus")) score += 2;
        if (text.contains("vie étudiante")) score += 3;
        return score;
    }

    private int calculateCatalogueScore(String text) {
        int score = 0;
        if (text.contains("catalogue")) score += 5;
        if (text.contains("liste des programmes")) score += 4;
        if (text.contains("offre de formation")) score += 4;
        return score;
    }

    private int calculateBourseScore(String text, String fileName) {
        int score = 0;
        if (text.contains("bourse")) score += 5;
        if (fileName.contains("bourse")) score += 5;
        if (text.contains("scholarship")) score += 5;
        if (text.contains("aide financière")) score += 4;
        return score;
    }

    private int calculateTranscriptScore(String text, String fileName) {
        int score = 0;
        if (text.contains("relevé de notes")) score += 5;
        if (fileName.contains("bulletin")) score += 4;
        if (text.contains("moyenne générale")) score += 3;
        if (text.contains("crédits")) score += 2;
        return score;
    }
}
