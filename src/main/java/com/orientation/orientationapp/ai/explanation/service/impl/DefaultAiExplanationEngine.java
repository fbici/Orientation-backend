package com.orientation.orientationapp.ai.explanation.service.impl;

import com.orientation.orientationapp.ai.explanation.model.AiExplanation;
import com.orientation.orientationapp.ai.explanation.service.AiExplanationEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DefaultAiExplanationEngine implements AiExplanationEngine {

    @Override
    public AiExplanation generateExplanation(String programName, String universityName,
                                              double score, String profileSummary,
                                              List<String> criteriaMet, List<String> criteriaFailed) {

        log.info("Generating AI explanation for {} at {}", programName, universityName);

        List<AiExplanation.ExplanationPoint> strengths = new ArrayList<>();
        List<AiExplanation.ExplanationPoint> criteria = new ArrayList<>();
        List<AiExplanation.ExplanationPoint> warnings = new ArrayList<>();

        // Generate strengths from criteria met
        for (String criterion : criteriaMet) {
            strengths.add(AiExplanation.ExplanationPoint.builder()
                    .category("strength")
                    .text("Vos résultats satisfont: " + criterion)
                    .detail("Ce critère est rempli avec succès")
                    .positive(true)
                    .impact("Positif")
                    .icon("✅")
                    .build());
        }

        // Generate warnings from criteria failed
        for (String criterion : criteriaFailed) {
            warnings.add(AiExplanation.ExplanationPoint.builder()
                    .category("warning")
                    .text("Critère non rempli: " + criterion)
                    .detail("Ce critère n'est pas satisfait")
                    .positive(false)
                    .impact("Négatif")
                    .icon("⚠️")
                    .build());
        }

        // Generate personalized advice based on score
        String advice;
        if (score >= 80) {
            advice = "Excellent profil ! Vous avez de très grandes chances d'admission. " +
                     "Nous vous recommandons vivement de postuler à " + programName + ".";
        } else if (score >= 60) {
            advice = "Bon profil. Vous avez de bonnes chances d'admission. " +
                     "Concentrez-vous sur l'amélioration de vos notes dans les matières clés.";
        } else {
            advice = "Profil à améliorer. " + programName + " est un choix ambitieux. " +
                     "Considérez des programmes alternatifs ou améliorez votre dossier.";
        }

        return AiExplanation.builder()
                .headline("Recommandation: " + programName + " - " + universityName)
                .summary(generateSummary(score, programName, universityName))
                .strengths(strengths)
                .criteria(criteria)
                .warnings(warnings)
                .alternatives(List.of())
                .personalizedAdvice(advice)
                .confidenceScore(score / 100.0)
                .build();
    }

    @Override
    public AiExplanation generateComparison(String programA, String programB,
                                              double scoreA, double scoreB) {
        List<AiExplanation.ExplanationPoint> comparison = new ArrayList<>();

        if (scoreA > scoreB) {
            comparison.add(AiExplanation.ExplanationPoint.builder()
                    .category("comparison")
                    .text(programA + " est meilleur de " + String.format("%.1f", scoreA - scoreB) + " points")
                    .detail("Score A: " + String.format("%.1f", scoreA) + " | Score B: " + String.format("%.1f", scoreB))
                    .positive(true)
                    .icon("📊")
                    .build());
        } else {
            comparison.add(AiExplanation.ExplanationPoint.builder()
                    .category("comparison")
                    .text(programB + " est meilleur de " + String.format("%.1f", scoreB - scoreA) + " points")
                    .detail("Score A: " + String.format("%.1f", scoreA) + " | Score B: " + String.format("%.1f", scoreB))
                    .positive(false)
                    .icon("📊")
                    .build());
        }

        return AiExplanation.builder()
                .headline("Comparaison: " + programA + " vs " + programB)
                .summary("Analyse comparative des deux programmes")
                .strengths(comparison)
                .confidenceScore(Math.max(scoreA, scoreB) / 100.0)
                .build();
    }

    private String generateSummary(double score, String programName, String universityName) {
        if (score >= 80) {
            return "Score excellent (" + String.format("%.1f", score) + "/100). " +
                   programName + " à " + universityName + " est fortement recommandé.";
        } else if (score >= 60) {
            return "Bon score (" + String.format("%.1f", score) + "/100). " +
                   programName + " est un bon choix.";
        } else {
            return "Score moyen (" + String.format("%.1f", score) + "/100). " +
                   "Des alternatives pourraient être plus adaptées.";
        }
    }
}
