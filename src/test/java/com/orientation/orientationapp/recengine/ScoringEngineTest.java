package com.orientation.orientationapp.recengine;

import com.orientation.orientationapp.recengine.profile.analyzer.impl.DefaultProfileAnalyzer;
import com.orientation.orientationapp.recengine.profile.model.AcademicProfile;
import com.orientation.orientationapp.recengine.scoring.engine.impl.DefaultScoringEngine;
import com.orientation.orientationapp.recengine.scoring.model.CompositeScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ScoringEngineTest {

    private DefaultScoringEngine scoringEngine;
    private DefaultProfileAnalyzer profileAnalyzer;

    @BeforeEach
    void setUp() {
        scoringEngine = new DefaultScoringEngine();
        profileAnalyzer = new DefaultProfileAnalyzer();
    }

    @Test
    void shouldComputeScoreForStrongProfile() {
        AcademicProfile profile = profileAnalyzer.analyze(
                "SCIENTIFIQUE",
                BigDecimal.valueOf(16),
                Map.of("Mathématiques", BigDecimal.valueOf(18), "Physique", BigDecimal.valueOf(16))
        );

        Map<String, Object> programData = Map.of("name", "Licence Informatique");
        CompositeScore score = scoringEngine.computeScore(profile, programData);

        assertNotNull(score);
        assertNotNull(score.getFinalScore());
        assertTrue(score.getFinalScore().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(score.getComponents());
        assertFalse(score.getComponents().isEmpty());
    }

    @Test
    void shouldComputeScoreForWeakProfile() {
        AcademicProfile profile = profileAnalyzer.analyze(
                "LITTERAIRE",
                BigDecimal.valueOf(8),
                Map.of("Arabe", BigDecimal.valueOf(9), "Français", BigDecimal.valueOf(8))
        );

        Map<String, Object> programData = Map.of("name", "Licence Informatique");
        CompositeScore score = scoringEngine.computeScore(profile, programData);

        assertNotNull(score);
        assertTrue(score.getFinalScore().compareTo(BigDecimal.valueOf(50)) < 0);
    }

    @Test
    void shouldIncludeAllScoreComponents() {
        AcademicProfile profile = profileAnalyzer.analyze(
                "SCIENTIFIQUE",
                BigDecimal.valueOf(14),
                Map.of("Mathématiques", BigDecimal.valueOf(15))
        );

        CompositeScore score = scoringEngine.computeScore(profile, Map.of());

        assertNotNull(score.getComponents());
        assertTrue(score.getComponents().size() >= 5);
    }
}
