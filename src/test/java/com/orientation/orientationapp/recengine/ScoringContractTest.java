package com.orientation.orientationapp.recengine;
import com.orientation.orientationapp.recengine.scoring.engine.impl.DefaultScoringEngine;
import com.orientation.orientationapp.recengine.profile.model.AcademicProfile;
import com.orientation.orientationapp.modules.orientation.entity.*;
import com.orientation.orientationapp.modules.university.entity.Country;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ScoringContractTest {
    AcademicProfile profile() {
        var scale=GradeScale.builder().country(new Country()).academicYear(new AcademicYear()).minScore(BigDecimal.ZERO)
                .maxScore(new BigDecimal("100")).normalizeTo(new BigDecimal("20")).build();
        return AcademicProfile.builder().generalAverage(new BigDecimal("60")).gradeScale(scale).build();
    }
    @Test void numericScoreUsesConfiguredScale() {
        var s=new DefaultScoringEngine().computeScore(profile(),Map.of());
        assertEquals(0,s.getFinalScore().compareTo(new BigDecimal("60")));
        assertEquals(0,s.getConfidence().compareTo(new BigDecimal("50")));
    }
    @Test void missingDimensionsRemainUncomputed() {
        var s=new DefaultScoringEngine().computeScore(profile(),Map.of());
        assertEquals(4,s.getComponents().stream().filter(c->!c.isComputed()&&c.getScore()==null).count());
    }
    @Test void absentGradesNeverGenerateDefaultScore() {
        var s=new DefaultScoringEngine().computeScore(AcademicProfile.builder().build(),Map.of());
        assertNull(s.getFinalScore());assertEquals(0,s.getConfidence().signum());
    }
    @Test void determinismDoesNotDependOnMapIteration() {
        var engine=new DefaultScoringEngine();var p=profile();p.setPreferredFields(List.of("SCIENCE"));
        var a=engine.computeScore(p,Map.of("studyField","SCIENCE","language","fr"));
        var b=engine.computeScore(p,new TreeMap<>(Map.of("language","fr","studyField","SCIENCE")));
        assertEquals(a.getFinalScore(),b.getFinalScore());
    }
    @Test void preferencesRequireAnActualMatch() {
        var p=profile();p.setPreferredFields(List.of("SCIENCE"));
        var score=new DefaultScoringEngine().computeScore(p,Map.of("studyField","LAW"));
        assertEquals(0,score.getComponents().stream().filter(c->c.getScoreType().equals("preference")).findFirst().orElseThrow().getScore().signum());
    }
    @Test void zeroTotalWeightsAreRejected() {
        assertThrows(IllegalArgumentException.class,()->new DefaultScoringEngine(BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO));
    }
    @Test void negativeWeightsAreRejected() {
        assertThrows(IllegalArgumentException.class,()->new DefaultScoringEngine(new BigDecimal("-1"),BigDecimal.ONE,BigDecimal.ONE,BigDecimal.ONE,BigDecimal.ONE));
    }
    @Test void missingRequiredSubjectIsNotFabricated() {
        var p=profile();p.setSubjectGrades(Map.of("MATH",new BigDecimal("90")));
        var s=new DefaultScoringEngine().computeScore(p,Map.of("subjectCodes",List.of("MATH","CHEM")));
        assertFalse(s.getComponents().get(1).isComputed());
    }
}
