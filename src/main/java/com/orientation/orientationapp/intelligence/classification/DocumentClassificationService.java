package com.orientation.orientationapp.intelligence.classification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service de classification automatique des documents.
 *
 * Analyse le contenu textuel pour déterminer le type de document :
 * - GUIDE: guide d'orientation universitaire
 * - REGLEMENT: règlement d'admission
 * - BROCHURE: brochure promotionnelle
 * - CATALOGUE: catalogue de programmes
 * - BOURSE: document de bourse/aide financière
 * - TRANSCRIPT: relevé de notes
 * - OTHER: autre
 */
@Slf4j
@Service
public class DocumentClassificationService {

    /**
     * Classifie un document basé sur son contenu texte.
     *
     * @param text texte extrait du document
     * @param fileName nom du fichier
     * @return type de document classifié
     */
    public String classify(String text, String fileName) {
        String lower = text.toLowerCase();
        String fileLower = fileName.toLowerCase();

        // Score par type de document
        int guideScore = 0;
        int reglementScore = 0;
        int brochureScore = 0;
        int catalogueScore = 0;
        int bourseScore = 0;
        int transcriptScore = 0;

        // --- GUIDE ---
        if (lower.contains("guide") || fileLower.contains("guide")) guideScore += 5;
        if (lower.contains("orientation")) guideScore += 3;
        if (lower.contains("admission")) guideScore += 2;
        if (lower.contains("inscription")) guideScore += 2;
        if (lower.contains("candidature")) guideScore += 2;
        if (lower.contains("rentrée")) guideScore += 2;
        if (lower.contains("calendrier")) guideScore += 1;

        // --- REGLEMENT ---
        if (lower.contains("règlement") || lower.contains("reglement")) reglementScore += 5;
        if (lower.contains("article")) reglementScore += 3;
        if (lower.contains("conditions d'admission")) reglementScore += 4;
        if (lower.contains("critères de sélection")) reglementScore += 4;
        if (lower.contains("obligatoire")) reglementScore += 2;

        // --- BROCHURE ---
        if (lower.contains("brochure")) brochureScore += 5;
        if (lower.contains("présentation")) brochureScore += 3;
        if (lower.contains("campus")) brochureScore += 2;
        if (lower.contains("vie étudiante")) brochureScore += 3;
        if (lower.contains("témoignage")) brochureScore += 2;

        // --- CATALOGUE ---
        if (lower.contains("catalogue")) catalogueScore += 5;
        if (lower.contains("liste des programmes")) catalogueScore += 4;
        if (lower.contains("offre de formation")) catalogueScore += 4;
        if (lower.contains("maquette")) catalogueScore += 2;

        // --- BOURSE ---
        if (lower.contains("bourse")) bourseScore += 5;
        if (lower.contains("scholarship")) bourseScore += 5;
        if (lower.contains("aide financière")) bourseScore += 4;
        if (lower.contains("allocation")) bourseScore += 3;
        if (lower.contains("financement")) bourseScore += 3;

        // --- TRANSCRIPT ---
        if (lower.contains("relevé de notes")) transcriptScore += 5;
        if (lower.contains("transcript")) transcriptScore += 5;
        if (lower.contains("bulletin")) transcriptScore += 4;
        if (lower.contains("moyenne générale")) transcriptScore += 3;
        if (lower.contains("crédits")) transcriptScore += 2;

        // Trouver le score maximum
        int maxScore = Math.max(guideScore,
                Math.max(reglementScore,
                Math.max(brochureScore,
                Math.max(catalogueScore,
                Math.max(bourseScore, transcriptScore)))));

        if (maxScore == 0) return "OTHER";

        if (maxScore == guideScore) return "GUIDE";
        if (maxScore == reglementScore) return "REGLEMENT";
        if (maxScore == brochureScore) return "BROCHURE";
        if (maxScore == catalogueScore) return "CATALOGUE";
        if (maxScore == bourseScore) return "BOURSE";
        if (maxScore == transcriptScore) return "TRANSCRIPT";

        return "OTHER";
    }
}
