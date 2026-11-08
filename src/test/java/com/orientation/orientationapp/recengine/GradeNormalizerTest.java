package com.orientation.orientationapp.recengine;

import com.orientation.orientationapp.modules.orientation.entity.*;
import com.orientation.orientationapp.modules.university.entity.Country;
import com.orientation.orientationapp.recengine.profile.normalization.GradeNormalizer;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class GradeNormalizerTest {
    GradeNormalizer normalizer=new GradeNormalizer();
    GradeScale scale(String maximum) {
        return GradeScale.builder().country(new Country()).academicYear(new AcademicYear())
                .minScore(BigDecimal.ZERO).maxScore(new BigDecimal(maximum)).normalizeTo(new BigDecimal("20")).build();
    }
    @Test void twentyIsPreserved() {assertEquals(0,normalizer.convert(new BigDecimal("12"),scale("20"),scale("20")).orElseThrow().compareTo(new BigDecimal("12")));}
    @Test void hundredConvertsOnlyWithKnownConfiguration() {assertEquals(0,normalizer.convert(new BigDecimal("60"),scale("100"),scale("20")).orElseThrow().compareTo(new BigDecimal("12")));}
    @Test void missingScaleIsUnknown() {assertTrue(normalizer.convert(BigDecimal.ONE,null,scale("20")).isEmpty());}
    @Test void unknownValueIsUnknown() {assertTrue(normalizer.convert(null,scale("100"),scale("20")).isEmpty());}
    @Test void outOfRangeNeverClamps() {assertTrue(normalizer.convert(new BigDecimal("101"),scale("100"),scale("20")).isEmpty());}
    @Test void negativeIsRejected() {assertTrue(normalizer.convert(new BigDecimal("-1"),scale("20"),scale("20")).isEmpty());}
    @Test void inactiveIsRejected() {var s=scale("20");s.setActive(false);assertTrue(normalizer.convert(BigDecimal.TEN,s,s).isEmpty());}
    @Test void incompatibleNormalizationTargetsStayUnknown() {var s=scale("100");s.setNormalizeTo(new BigDecimal("4"));assertTrue(normalizer.convert(BigDecimal.TEN,s,scale("20")).isEmpty());}
    @Test void invalidRangeIsRejected() {var s=scale("20");s.setMinScore(new BigDecimal("20"));assertTrue(normalizer.convert(BigDecimal.TEN,s,s).isEmpty());}
    @Test void comparisonDoesNotRoundAcrossBoundary() {assertTrue(normalizer.compare(new BigDecimal("59.999999999999"),scale("100"),new BigDecimal("12"),scale("20")).orElseThrow()<0);}
    @Test void exactThresholdMatches() {assertEquals(0,normalizer.compare(new BigDecimal("60"),scale("100"),new BigDecimal("12"),scale("20")).orElseThrow());}
    @Test void recurringRatioUsesExactCrossProduct() {assertEquals(0,normalizer.compare(BigDecimal.ONE,scale("3"),new BigDecimal("2"),scale("6")).orElseThrow());}
    @Test void explicitLetterMappingConverts() {
        var source=scale("20");
        source.setItems(java.util.Set.of(GradeScaleItem.builder().rawGrade("A").normalizedValue(new BigDecimal("16")).build()));
        assertEquals(0,normalizer.convertLabel("A",source,scale("100")).orElseThrow().compareTo(new BigDecimal("80")));
    }
    @Test void unknownLetterStaysUnknown() {
        assertTrue(normalizer.convertLabel("A",scale("20"),scale("100")).isEmpty());
    }
}
