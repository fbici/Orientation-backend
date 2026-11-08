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

        AcademicProfile profile = analyzer.analyze("SCIENTIFIQUE", BigDecimal.valueOf(15.3), grades,scale());

        assertNotNull(profile);
        assertEquals("SCIENTIFIQUE", profile.getBacType());
        assertEquals(0, BigDecimal.valueOf(15.3).compareTo(profile.getBacAverage()));
        assertNotNull(profile.getGeneralAverage());
        assertNotNull(profile.getStrongSubjects());
        assertNotNull(profile.getWeakSubjects());
        assertNull(profile.getCompetencyScores());
        assertNull(profile.getDominantCompetency());
    }

    @Test
    void shouldIdentifyStrongSubjects() {
        Map<String, BigDecimal> grades = Map.of(
                "Mathématiques", BigDecimal.valueOf(18),
                "Physique", BigDecimal.valueOf(16),
                "Arabe", BigDecimal.valueOf(10)
        );

        AcademicProfile profile = analyzer.analyze("SCIENTIFIQUE", BigDecimal.valueOf(14.7), grades,scale());

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

        AcademicProfile profile = analyzer.analyze("SCIENTIFIQUE", BigDecimal.valueOf(11), grades,scale());

        assertTrue(profile.getWeakSubjects().contains("Physique"));
        assertTrue(profile.getWeakSubjects().contains("Arabe"));
        assertFalse(profile.getWeakSubjects().contains("Mathématiques"));
    }
    private com.orientation.orientationapp.modules.orientation.entity.GradeScale scale() {
        return com.orientation.orientationapp.modules.orientation.entity.GradeScale.builder()
            .country(new com.orientation.orientationapp.modules.university.entity.Country())
            .academicYear(new com.orientation.orientationapp.modules.orientation.entity.AcademicYear())
            .minScore(BigDecimal.ZERO).maxScore(new BigDecimal("20")).normalizeTo(new BigDecimal("20")).build();
    }
    @Test void unscaledLegacyInputsNeverAssumeTwenty() {
        var p=analyzer.analyze("SCIENTIFIQUE",new BigDecimal("80"),Map.of("MATH",new BigDecimal("90")));
        assertNull(p.getGeneralAverage());assertNull(p.getNormalizedScore());assertNull(p.getCompetencyScores());
    }
}
