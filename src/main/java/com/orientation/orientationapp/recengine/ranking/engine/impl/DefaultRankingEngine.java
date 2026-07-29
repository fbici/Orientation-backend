package com.orientation.orientationapp.recengine.ranking.engine.impl;

import com.orientation.orientationapp.recengine.explanation.model.Explanation;
import com.orientation.orientationapp.recengine.ranking.model.RankedRecommendation;
import com.orientation.orientationapp.recengine.scoring.model.CompositeScore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DefaultRankingEngine {

    public List<RankedRecommendation> rank(List<CompositeScore> scores, List<Map<String, Object>> programs) {
        List<RankedRecommendation> recommendations = new ArrayList<>();

        for (int i = 0; i < Math.min(scores.size(), programs.size()); i++) {
            CompositeScore score = scores.get(i);
            Map<String, Object> program = programs.get(i);

            RankedRecommendation rec = RankedRecommendation.builder()
                    .programId(UUID.randomUUID())
                    .programName((String) program.getOrDefault("name", "Programme"))
                    .universityName((String) program.getOrDefault("university", "Université"))
                    .facultyName((String) program.getOrDefault("faculty", "Faculté"))
                    .score(score)
                    .confidence(score.getConfidence())
                    .admissionProbability(computeProbability(score.getFinalScore()))
                    .difficultyLevel(computeDifficulty(score.getFinalScore()))
                    .eligible(score.getFinalScore().compareTo(BigDecimal.valueOf(40)) >= 0)
                    .build();

            recommendations.add(rec);
        }

        // Sort by score
        recommendations.sort((a, b) -> b.getScore().getFinalScore().compareTo(a.getScore().getFinalScore()));

        // Assign ranks
        for (int i = 0; i < recommendations.size(); i++) {
            recommendations.get(i).setRank(i + 1);
        }

        return recommendations;
    }

    public List<RankedRecommendation> rankWithExplanations(List<CompositeScore> scores, List<Map<String, Object>> programs) {
        List<RankedRecommendation> ranked = rank(scores, programs);

        for (RankedRecommendation rec : ranked) {
            Explanation explanation = buildExplanation(rec);
            rec.setExplanation(explanation);
        }

        return ranked;
    }

    private BigDecimal computeProbability(BigDecimal score) {
        // Simple probability calculation based on score
        if (score.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return BigDecimal.valueOf(85);
        } else if (score.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return BigDecimal.valueOf(65);
        } else if (score.compareTo(BigDecimal.valueOf(40)) >= 0) {
            return BigDecimal.valueOf(40);
        } else {
            return BigDecimal.valueOf(15);
        }
    }

    private String computeDifficulty(BigDecimal score) {
        if (score.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return "Facile";
        } else if (score.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "Moyen";
        } else if (score.compareTo(BigDecimal.valueOf(40)) >= 0) {
            return "Difficile";
        } else {
            return "Très difficile";
        }
    }

    private Explanation buildExplanation(RankedRecommendation rec) {
        List<Explanation.ExplanationItem> strengths = new ArrayList<>();
        List<Explanation.ExplanationItem> criteria = new ArrayList<>();

        // Analyze score components
        if (rec.getScore() != null && rec.getScore().getComponents() != null) {
            for (var component : rec.getScore().getComponents()) {
                if (component.getScore().compareTo(BigDecimal.valueOf(70)) >= 0) {
                    strengths.add(Explanation.ExplanationItem.builder()
                            .category(component.getScoreType())
                            .text(component.getExplanation())
                            .positive(true)
                            .impact("Positif")
                            .build());
                } else {
                    criteria.add(Explanation.ExplanationItem.builder()
                            .category(component.getScoreType())
                            .text(component.getExplanation())
                            .positive(false)
                            .impact("À améliorer")
                            .build());
                }
            }
        }

        String summary = rec.getScore() != null ? rec.getScore().getSummary() : "Analyse en cours";

        return Explanation.builder()
                .summary(summary)
                .strengths(strengths)
                .criteria(criteria)
                .recommendation("Recommandation basée sur l'analyse de votre profil académique")
                .build();
    }
}
