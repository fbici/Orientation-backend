package com.orientation.orientationapp.recengine.explanation.engine.impl;

import com.orientation.orientationapp.recengine.explanation.model.Explanation;
import com.orientation.orientationapp.recengine.profile.model.AcademicProfile;
import com.orientation.orientationapp.recengine.scoring.model.CompositeScore;
import com.orientation.orientationapp.recengine.scoring.model.ScoreDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DefaultExplanationEngine {

    public Explanation explain(AcademicProfile profile, CompositeScore score, Map<String, Object> programData) {
        List<Explanation.ExplanationItem> strengths = new ArrayList<>();
        List<Explanation.ExplanationItem> criteria = new ArrayList<>();
        List<Explanation.ExplanationItem> warnings = new ArrayList<>();

        // Analyze each score component
        if (score.getComponents() != null) {
            for (ScoreDetail component : score.getComponents()) {
                Explanation.ExplanationItem item = Explanation.ExplanationItem.builder()
                        .category(component.getScoreType())
                        .text(component.getExplanation())
                        .positive(component.getScore().compareTo(BigDecimal.valueOf(60)) >= 0)
                        .impact(component.getScore().compareTo(BigDecimal.valueOf(70)) >= 0 ? "Fort" : "Moyen")
                        .build();

                if (item.isPositive()) {
                    strengths.add(item);
                } else {
                    criteria.add(item);
                }
            }
        }

        // Generate warnings
        if (profile.getBacAverage() != null && profile.getBacAverage().compareTo(BigDecimal.valueOf(10)) < 0) {
            warnings.add(Explanation.ExplanationItem.builder()
                    .category("academic")
                    .text("Votre moyenne est inférieure à 10/20")
                    .positive(false)
                    .impact("Critique")
                    .build());
        }

        if (profile.getWeakSubjects() != null && !profile.getWeakSubjects().isEmpty()) {
            warnings.add(Explanation.ExplanationItem.builder()
                    .category("subjects")
                    .text("Matières faibles: " + String.join(", ", profile.getWeakSubjects()))
                    .positive(false)
                    .impact("Important")
                    .build());
        }

        String summary = buildSummary(strengths, criteria, warnings, score);

        return Explanation.builder()
                .summary(summary)
                .strengths(strengths)
                .criteria(criteria)
                .warnings(warnings)
                .recommendation(buildRecommendation(strengths, profile))
                .build();
    }

    private String buildSummary(List<Explanation.ExplanationItem> strengths,
                                 List<Explanation.ExplanationItem> criteria,
                                 List<Explanation.ExplanationItem> warnings,
                                 CompositeScore score) {
        StringBuilder sb = new StringBuilder();

        if (score.getFinalScore().compareTo(BigDecimal.valueOf(80)) >= 0) {
            sb.append("Excellent profil. ");
        } else if (score.getFinalScore().compareTo(BigDecimal.valueOf(60)) >= 0) {
            sb.append("Bon profil. ");
        } else {
            sb.append("Profil à améliorer. ");
        }

        sb.append(strengths.size()).append(" point(s) fort(s) identifié(s).");

        if (!warnings.isEmpty()) {
            sb.append(" ").append(warnings.size()).append(" avertissement(s).");
        }

        return sb.toString();
    }

    private String buildRecommendation(List<Explanation.ExplanationItem> strengths, AcademicProfile profile) {
        if (strengths.isEmpty()) {
            return "Concentrez-vous sur l'amélioration de vos notes.";
        }

        String dominantStrength = strengths.get(0).getCategory();
        return switch (dominantStrength) {
            case "academic" -> "Votre excellent dossier académique vous ouvre de nombreuses portes.";
            case "subject" -> "Vos matières fortes sont un atout majeur.";
            case "competency" -> "Vos compétences correspondent bien à ce programme.";
            default -> "Continuez à maintenir votre niveau.";
        };
    }
}
