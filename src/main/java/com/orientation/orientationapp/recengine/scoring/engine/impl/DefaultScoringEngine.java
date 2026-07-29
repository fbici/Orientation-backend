package com.orientation.orientationapp.recengine.scoring.engine.impl;

import com.orientation.orientationapp.recengine.profile.model.AcademicProfile;
import com.orientation.orientationapp.recengine.scoring.model.CompositeScore;
import com.orientation.orientationapp.recengine.scoring.model.ScoreDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DefaultScoringEngine {

    private static final Map<String, BigDecimal> WEIGHTS = Map.of(
            "academic", BigDecimal.valueOf(0.30),
            "subject", BigDecimal.valueOf(0.20),
            "scholarship", BigDecimal.valueOf(0.10),
            "competency", BigDecimal.valueOf(0.15),
            "preference", BigDecimal.valueOf(0.10),
            "country", BigDecimal.valueOf(0.05),
            "language", BigDecimal.valueOf(0.05),
            "historical", BigDecimal.valueOf(0.05)
    );

    public CompositeScore computeScore(AcademicProfile profile, Map<String, Object> programData) {
        List<ScoreDetail> components = new ArrayList<>();

        // Academic Score
        ScoreDetail academicScore = computeAcademicScore(profile);
        components.add(academicScore);

        // Subject Score
        ScoreDetail subjectScore = computeSubjectScore(profile, programData);
        components.add(subjectScore);

        // Competency Score
        ScoreDetail competencyScore = computeCompetencyScore(profile, programData);
        components.add(competencyScore);

        // Preference Score
        ScoreDetail preferenceScore = computePreferenceScore(profile, programData);
        components.add(preferenceScore);

        // Country Score
        ScoreDetail countryScore = computeCountryScore(profile, programData);
        components.add(countryScore);

        // Language Score
        ScoreDetail languageScore = computeLanguageScore(profile, programData);
        components.add(languageScore);

        // Historical Score (placeholder)
        ScoreDetail historicalScore = ScoreDetail.builder()
                .scoreType("historical")
                .score(BigDecimal.valueOf(50))
                .weight(WEIGHTS.get("historical"))
                .explanation("Données historiques limitées")
                .computed(true)
                .build();
        components.add(historicalScore);

        // Compute composite score
        BigDecimal finalScore = components.stream()
                .map(s -> s.getScore().multiply(s.getWeight()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal confidence = computeConfidence(components);

        return CompositeScore.builder()
                .finalScore(finalScore)
                .confidence(confidence)
                .components(components)
                .summary(buildSummary(finalScore, confidence))
                .build();
    }

    private ScoreDetail computeAcademicScore(AcademicProfile profile) {
        BigDecimal score = BigDecimal.ZERO;
        String explanation = "";

        if (profile.getBacAverage() != null) {
            // Normalize to 0-100
            score = profile.getBacAverage().multiply(BigDecimal.valueOf(5));
            if (score.compareTo(BigDecimal.valueOf(100)) > 0) {
                score = BigDecimal.valueOf(100);
            }

            if (profile.getBacAverage().compareTo(BigDecimal.valueOf(16)) >= 0) {
                explanation = "Moyenne excellente (" + profile.getBacAverage() + "/20)";
            } else if (profile.getBacAverage().compareTo(BigDecimal.valueOf(14)) >= 0) {
                explanation = "Bonne moyenne (" + profile.getBacAverage() + "/20)";
            } else if (profile.getBacAverage().compareTo(BigDecimal.valueOf(12)) >= 0) {
                explanation = "Moyenne correcte (" + profile.getBacAverage() + "/20)";
            } else {
                explanation = "Moyenne insuffisante (" + profile.getBacAverage() + "/20)";
            }
        }

        return ScoreDetail.builder()
                .scoreType("academic")
                .score(score)
                .weight(WEIGHTS.get("academic"))
                .explanation(explanation)
                .computed(true)
                .build();
    }

    private ScoreDetail computeSubjectScore(AcademicProfile profile, Map<String, Object> programData) {
        BigDecimal score = BigDecimal.valueOf(50);
        String explanation = "Score moyen par matière";

        if (profile.getStrongSubjects() != null && !profile.getStrongSubjects().isEmpty()) {
            score = score.add(BigDecimal.valueOf(profile.getStrongSubjects().size() * 5));
            explanation = profile.getStrongSubjects().size() + " matières fortes identifiées";
        }

        if (score.compareTo(BigDecimal.valueOf(100)) > 0) {
            score = BigDecimal.valueOf(100);
        }

        return ScoreDetail.builder()
                .scoreType("subject")
                .score(score)
                .weight(WEIGHTS.get("subject"))
                .explanation(explanation)
                .computed(true)
                .build();
    }

    private ScoreDetail computeCompetencyScore(AcademicProfile profile, Map<String, Object> programData) {
        BigDecimal score = BigDecimal.valueOf(50);
        String explanation = "Compétences évaluées";

        if (profile.getCompetencyScores() != null && !profile.getCompetencyScores().isEmpty()) {
            BigDecimal maxCompetency = profile.getCompetencyScores().values().stream()
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            score = maxCompetency.multiply(BigDecimal.valueOf(10));
            if (score.compareTo(BigDecimal.valueOf(100)) > 0) {
                score = BigDecimal.valueOf(100);
            }
            explanation = "Compétence dominante: " + profile.getDominantCompetency();
        }

        return ScoreDetail.builder()
                .scoreType("competency")
                .score(score)
                .weight(WEIGHTS.get("competency"))
                .explanation(explanation)
                .computed(true)
                .build();
    }

    private ScoreDetail computePreferenceScore(AcademicProfile profile, Map<String, Object> programData) {
        BigDecimal score = BigDecimal.valueOf(50);
        String explanation = "Préférences non spécifiées";

        if (profile.getPreferredFields() != null && !profile.getPreferredFields().isEmpty()) {
            score = BigDecimal.valueOf(70);
            explanation = profile.getPreferredFields().size() + " domaine(s) d'intérêt";
        }

        return ScoreDetail.builder()
                .scoreType("preference")
                .score(score)
                .weight(WEIGHTS.get("preference"))
                .explanation(explanation)
                .computed(true)
                .build();
    }

    private ScoreDetail computeCountryScore(AcademicProfile profile, Map<String, Object> programData) {
        BigDecimal score = BigDecimal.valueOf(50);
        String explanation = "Score pays par défaut";

        if (profile.getPreferredCountries() != null && !profile.getPreferredCountries().isEmpty()) {
            score = BigDecimal.valueOf(80);
            explanation = profile.getPreferredCountries().size() + " pays préféré(s)";
        }

        return ScoreDetail.builder()
                .scoreType("country")
                .score(score)
                .weight(WEIGHTS.get("country"))
                .explanation(explanation)
                .computed(true)
                .build();
    }

    private ScoreDetail computeLanguageScore(AcademicProfile profile, Map<String, Object> programData) {
        BigDecimal score = BigDecimal.valueOf(60);
        String explanation = "Score langue par défaut";

        if (profile.getLanguage() != null) {
            score = BigDecimal.valueOf(80);
            explanation = "Langue: " + profile.getLanguage();
        }

        return ScoreDetail.builder()
                .scoreType("language")
                .score(score)
                .weight(WEIGHTS.get("language"))
                .explanation(explanation)
                .computed(true)
                .build();
    }

    private BigDecimal computeConfidence(List<ScoreDetail> components) {
        long computedCount = components.stream().filter(ScoreDetail::isComputed).count();
        return BigDecimal.valueOf((double) computedCount / components.size() * 100)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String buildSummary(BigDecimal score, BigDecimal confidence) {
        if (score.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return "Excellent profil académique";
        } else if (score.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "Bon profil académique";
        } else if (score.compareTo(BigDecimal.valueOf(40)) >= 0) {
            return "Profil académique moyen";
        } else {
            return "Profil académique à améliorer";
        }
    }
}
