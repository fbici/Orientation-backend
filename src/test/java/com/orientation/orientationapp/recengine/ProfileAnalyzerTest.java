package com.orientation.orientationapp.recengine;

import com.orientation.orientationapp.recengine.profile.analyzer.impl.DefaultProfileAnalyzer;
import com.orientation.orientationapp.recengine.profile.model.AcademicProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProfileAnalyzerTest {

    private DefaultProfileAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new DefaultProfileAnalyzer();
    }

    @Test
    void shouldBuildProfileFromGrades() {
        Map<String, BigDecimal> grades = Map.of(
                "Mathématiques", BigDecimal.valueOf(18),
                "Physique", BigDecimal.valueOf(16),
                "Arabe", BigDecimal.valueOf(12)
        );

        AcademicProfile profile = analyzer.analyze("SCIENTIFIQUE", BigDecimal.valueOf(15.3), grades);

        assertNotNull(profile);
        assertEquals("SCIENTIFIQUE", profile.getBacType());
        assertEquals(0, BigDecimal.valueOf(15.3).compareTo(profile.getBacAverage()));
        assertNotNull(profile.getGeneralAverage());
        assertNotNull(profile.getStrongSubjects());
        assertNotNull(profile.getWeakSubjects());
        assertNotNull(profile.getCompetencyScores());
        assertNotNull(profile.getDominantCompetency());
    }

    @Test
    void shouldIdentifyStrongSubjects() {
        Map<String, BigDecimal> grades = Map.of(
                "Mathématiques", BigDecimal.valueOf(18),
                "Physique", BigDecimal.valueOf(16),
                "Arabe", BigDecimal.valueOf(10)
        );

        AcademicProfile profile = analyzer.analyze("SCIENTIFIQUE", BigDecimal.valueOf(14.7), grades);

        assertTrue(profile.getStrongSubjects().contains("Mathématiques"));
        assertTrue(profile.getStrongSubjects().contains("Physique"));
        assertFalse(profile.getStrongSubjects().contains("Arabe"));
    }

    @Test
    void shouldIdentifyWeakSubjects() {
        Map<String, BigDecimal> grades = Map.of(
                "Mathématiques", BigDecimal.valueOf(18),
                "Physique", BigDecimal.valueOf(8),
                "Arabe", BigDecimal.valueOf(7)
        );

        AcademicProfile profile = analyzer.analyze("SCIENTIFIQUE", BigDecimal.valueOf(11), grades);

        assertTrue(profile.getWeakSubjects().contains("Physique"));
        assertTrue(profile.getWeakSubjects().contains("Arabe"));
        assertFalse(profile.getWeakSubjects().contains("Mathématiques"));
    }
}
