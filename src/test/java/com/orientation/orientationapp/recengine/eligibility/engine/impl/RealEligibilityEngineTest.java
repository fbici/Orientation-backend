package com.orientation.orientationapp.recengine.eligibility.engine.impl;

import com.orientation.orientationapp.common.enums.*;
import com.orientation.orientationapp.modules.orientation.entity.*;
import com.orientation.orientationapp.modules.orientation.repository.*;
import com.orientation.orientationapp.modules.scholarship.repository.ScholarshipCriterionRepository;
import com.orientation.orientationapp.modules.university.entity.*;
import com.orientation.orientationapp.recengine.eligibility.model.*;
import com.orientation.orientationapp.recengine.profile.model.AcademicProfile;
import com.orientation.orientationapp.recengine.profile.normalization.GradeNormalizer;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.orientation.orientationapp.recengine.eligibility.model.EligibilityStatus.*;

class RealEligibilityEngineTest {
    AdmissionCriterionRepository criteria = mock(AdmissionCriterionRepository.class);
    GradeScaleRepository scales = mock(GradeScaleRepository.class);
    com.orientation.orientationapp.docintel.document.repository.ExtractedFactRepository facts=mock(com.orientation.orientationapp.docintel.document.repository.ExtractedFactRepository.class);
    RealEligibilityEngine engine = new RealEligibilityEngine(criteria, mock(ScholarshipCriterionRepository.class), scales, new GradeNormalizer(),facts);
    AcademicProfile profile; GradeScale scale; GuideVersion version; Program programme; UUID year, tenant;
    @BeforeEach void fixture() {
        year=UUID.randomUUID();tenant=UUID.randomUUID();
        var country=new Country();country.setId(UUID.randomUUID());
        var academicYear=new AcademicYear();academicYear.setId(year);
        scale=GradeScale.builder().country(country).academicYear(academicYear).minScore(BigDecimal.ZERO)
                .maxScore(new BigDecimal("20")).normalizeTo(new BigDecimal("20")).build();
        scale.setId(UUID.randomUUID());
        var guide=OrientationGuide.builder().country(country).academicYear(academicYear).tenantId(tenant).build();
        version=GuideVersion.builder().orientationGuide(guide).effectiveDate(LocalDate.now().minusDays(1))
                .status(GuideVersionStatus.ACTIVE).build();version.setId(UUID.randomUUID());
        var uni=University.builder().verified(true).country(country).tenantId(tenant).build();
        var campus=Campus.builder().verified(true).university(uni).build();
        var faculty=Faculty.builder().verified(true).campus(campus).build();
        programme=Program.builder().verified(true).faculty(faculty).academicYearId(year).build();programme.setId(UUID.randomUUID());
        profile=AcademicProfile.builder().academicYearId(year).gradesAcademicYearId(year).tenantId(tenant)
                .gradeScale(scale).generalAverage(new BigDecimal("12")).bacType("SCI").build();
        when(scales.findByCountryIdAndAcademicYearId(country.getId(),year)).thenReturn(Optional.of(scale));
    }
    AdmissionCriterion rule() {
        var r=AdmissionCriterion.builder().program(programme).guideVersion(version).criterionType(CriterionType.MIN_AVERAGE)
                .operator(CriterionOperator.GREATER_THAN_OR_EQUAL).minValue(new BigDecimal("12"))
                .valueScaleMax(new BigDecimal("20")).sourceFactId(UUID.randomUUID()).build();r.setId(UUID.randomUUID());
        var fact=new com.orientation.orientationapp.docintel.document.entity.ExtractedFact();
        fact.setStatus("VALIDATED");fact.setPublishedAt(java.time.Instant.now());
        fact.setPublishedEntityId(r.getId());fact.setGuideVersionId(version.getId());
        fact.setDocumentVersion(com.orientation.orientationapp.docintel.document.entity.DocumentVersion.builder().active(true).build());
        when(facts.findById(r.getSourceFactId())).thenReturn(Optional.of(fact));
        return r;
    }
    EligibilityResult evaluate(AdmissionCriterion... rules) {return engine.evaluateCriteria(profile,List.of(rules));}
    @Test void exactBoundaryIsEligible() {assertEquals(ELIGIBLE,evaluate(rule()).getStatus());}
    @Test void roundingNeverPromotesCandidate() {profile.setGeneralAverage(new BigDecimal("11.999999999999"));assertEquals(NOT_ELIGIBLE,evaluate(rule()).getStatus());}
    @Test void missingAverageIsUnknown() {profile.setGeneralAverage(null);assertEquals(INSUFFICIENT_DATA,evaluate(rule()).getStatus());}
    @Test void missingScaleIsUnknown() {profile.setGradeScale(null);assertEquals(INSUFFICIENT_DATA,evaluate(rule()).getStatus());}
    @Test void noRuleIsUnknown() {assertEquals(INSUFFICIENT_DATA,evaluate().getStatus());}
    @Test void unsupportedCriterionIsUnknown() {var r=rule();r.setCriterionType(CriterionType.COEFFICIENT);assertEquals(INSUFFICIENT_DATA,evaluate(r).getStatus());}
    @Test void missingSubjectIsUnknown() {
        var r=rule();r.setCriterionType(CriterionType.MIN_GRADE);
        r.setSubject(Subject.builder().code("MATH").gradeScale(scale).build());
        assertEquals(INSUFFICIENT_DATA,evaluate(r).getStatus());
    }
    @Test void lowSubjectIsRefused() {
        var r=rule();r.setCriterionType(CriterionType.MIN_GRADE);
        r.setSubject(Subject.builder().code("MATH").gradeScale(scale).build());
        profile.setSubjectGrades(Map.of("MATH",new BigDecimal("11")));
        assertEquals(NOT_ELIGIBLE,evaluate(r).getStatus());
    }
    @Test void allowedSeriesPassesExactList() {
        var r=rule();r.setCriterionType(CriterionType.CUSTOM);r.setFieldName("bacType");
        r.setOperator(CriterionOperator.IN_LIST);r.setStringValue("SCI;TECH");
        assertEquals(ELIGIBLE,evaluate(r).getStatus());profile.setBacType("SC");assertEquals(NOT_ELIGIBLE,evaluate(r).getStatus());
    }
    @Test void unsupportedCustomNeverPasses() {
        var r=rule();r.setCriterionType(CriterionType.CUSTOM);r.setFieldName("arbitrary");r.setStringValue("SCI");
        assertEquals(INSUFFICIENT_DATA,evaluate(r).getStatus());
    }
    @Test void exclusionIsApplied() {
        var r=rule();r.setCriterionType(CriterionType.CUSTOM);r.setFieldName("bacType");
        r.setOperator(CriterionOperator.NOT_IN_LIST);r.setStringValue("SCI;TECH");
        assertEquals(NOT_ELIGIBLE,evaluate(r).getStatus());
    }
    @Test void andCombinesActualFailures() {
        var a=rule();var b=rule();b.setMinValue(new BigDecimal("13"));assertEquals(NOT_ELIGIBLE,evaluate(a,b).getStatus());
    }
    @Test void orCombinesRealAlternatives() {
        var a=rule();var b=rule();b.setMinValue(new BigDecimal("13"));
        a.setLogicalOperator(LogicalOperator.OR);b.setLogicalOperator(LogicalOperator.OR);
        assertEquals(ELIGIBLE,evaluate(a,b).getStatus());
    }
    @Test void unknownOrFalseStaysUnknown() {
        var a=rule();a.setCriterionType(CriterionType.COEFFICIENT);
        var b=rule();b.setMinValue(new BigDecimal("13"));a.setLogicalOperator(LogicalOperator.OR);b.setLogicalOperator(LogicalOperator.OR);
        assertEquals(INSUFFICIENT_DATA,evaluate(a,b).getStatus());
    }
    @Test void inconsistentOperatorsStayUnknown() {
        var a=rule();var b=rule();a.setLogicalOperator(LogicalOperator.OR);
        assertEquals(INSUFFICIENT_DATA,evaluate(a,b).getStatus());
    }
    @Test void wrongYearNeverPasses() {profile.setAcademicYearId(UUID.randomUUID());assertEquals(INSUFFICIENT_DATA,evaluate(rule()).getStatus());}
    @Test void wrongSourceYearNeverPasses() {profile.setGradesAcademicYearId(UUID.randomUUID());assertEquals(INSUFFICIENT_DATA,evaluate(rule()).getStatus());}
    @Test void archivedGuideNeverPasses() {version.setStatus(GuideVersionStatus.ARCHIVED);assertEquals(INSUFFICIENT_DATA,evaluate(rule()).getStatus());}
    @Test void futureGuideNeverPasses() {version.setEffectiveDate(LocalDate.now().plusDays(1));assertEquals(INSUFFICIENT_DATA,evaluate(rule()).getStatus());}
    @Test void expiredGuideNeverPasses() {version.setExpiryDate(LocalDate.now().minusDays(1));assertEquals(INSUFFICIENT_DATA,evaluate(rule()).getStatus());}
    @Test void unverifiedProgrammeNeverPasses() {programme.setVerified(false);assertEquals(INSUFFICIENT_DATA,evaluate(rule()).getStatus());}
    @Test void differentTenantNeverPasses() {profile.setTenantId(UUID.randomUUID());assertEquals(INSUFFICIENT_DATA,evaluate(rule()).getStatus());}
    @Test void differentCountryNeverPasses() {
        var c=new Country();c.setId(UUID.randomUUID());version.getOrientationGuide().setCountry(c);
        assertEquals(INSUFFICIENT_DATA,evaluate(rule()).getStatus());
    }
    @Test void explanationContainsRealSourceAndContext() {
        var r=rule();var explanation=evaluate(r).getCriteria().getFirst();
        assertEquals(r.getSourceFactId(),explanation.getSourceFactId());assertEquals(year,explanation.getAcademicYearId());
        assertEquals(version.getId(),explanation.getGuideVersionId());assertTrue(explanation.isMet());
    }
    @Test void absentVersionIsNeverQueriedAsNull() {
        assertEquals(INSUFFICIENT_DATA,engine.evaluateProgram(profile,programme.getId(),null).getStatus());
        verifyNoInteractions(criteria);
    }
    @Test void unknownNumericScaleIsUnknown() {var r=rule();r.setValueScaleMax(null);assertEquals(INSUFFICIENT_DATA,evaluate(r).getStatus());}
    @Test void optionalRulesDoNotDeclareAdmission() {var r=rule();r.setMandatory(false);assertEquals(INSUFFICIENT_DATA,evaluate(r).getStatus());}
    @Test void optionalFailureDoesNotBecomeBlocking() {
        var a=rule();var optional=rule();optional.setMandatory(false);optional.setMinValue(new BigDecimal("15"));
        var result=evaluate(a,optional);assertEquals(ELIGIBLE,result.getStatus());
        assertTrue(result.getBlockingIssues().isEmpty());assertFalse(result.getWarnings().isEmpty());
    }
    @Test void sourceWithdrawalStopsEligibility() {
        var r=rule();facts.findById(r.getSourceFactId()).orElseThrow().setStatus("REJECTED");
        assertEquals(INSUFFICIENT_DATA,evaluate(r).getStatus());
    }
    @Test void unpublishedSourceStopsEligibility() {
        var r=rule();facts.findById(r.getSourceFactId()).orElseThrow().setPublishedAt(null);
        assertEquals(INSUFFICIENT_DATA,evaluate(r).getStatus());
    }
    @Test void inactiveDocumentVersionStopsEligibility() {
        var r=rule();facts.findById(r.getSourceFactId()).orElseThrow().getDocumentVersion().setActive(false);
        assertEquals(INSUFFICIENT_DATA,evaluate(r).getStatus());
    }
    @Test void inactiveFacultyCannotBeRecommended() {
        programme.getFaculty().setActive(false);assertEquals(INSUFFICIENT_DATA,evaluate(rule()).getStatus());
    }
    @Test void subjectScaleFromAnotherYearIsNotApplied() {
        var r=rule();r.setCriterionType(CriterionType.MIN_GRADE);
        var otherYear=new AcademicYear();otherYear.setId(UUID.randomUUID());
        var wrong=GradeScale.builder().country(scale.getCountry()).academicYear(otherYear)
            .minScore(BigDecimal.ZERO).maxScore(new BigDecimal("20")).normalizeTo(new BigDecimal("20")).build();
        r.setSubject(Subject.builder().code("MATH").gradeScale(wrong).build());
        profile.setSubjectGrades(Map.of("MATH",new BigDecimal("15")));
        assertEquals(INSUFFICIENT_DATA,evaluate(r).getStatus());
    }
    @Test void conversionIsExplainedAndComparedWithoutRounding() {
        var source=GradeScale.builder().country(scale.getCountry()).academicYear(scale.getAcademicYear())
            .minScore(BigDecimal.ZERO).maxScore(new BigDecimal("100")).normalizeTo(new BigDecimal("20")).build();
        profile.setGradeScale(source);profile.setGeneralAverage(new BigDecimal("60"));
        var result=evaluate(rule());assertEquals(ELIGIBLE,result.getStatus());
        var trace=result.getCriteria().getFirst().getNormalization();
        assertEquals(new BigDecimal("60"),trace.get("originalValue"));
        assertEquals(0,((BigDecimal)trace.get("convertedValue")).compareTo(new BigDecimal("12")));
        assertEquals("EXACT_CROSS_MULTIPLICATION",trace.get("comparisonMethod"));
    }
    @Test void nationalBacSeriesIsDistinctFromItsGeneralCategory() {
        var r=rule();r.setCriterionType(CriterionType.CUSTOM);r.setFieldName("bacSeries");
        r.setOperator(CriterionOperator.IN_LIST);r.setStringValue("C;D");
        profile.setBacType("SCIENTIFIQUE");
        assertEquals(INSUFFICIENT_DATA,evaluate(r).getStatus());
        profile.setBacSeries("D");assertEquals(ELIGIBLE,evaluate(r).getStatus());
        profile.setBacSeries("A");assertEquals(NOT_ELIGIBLE,evaluate(r).getStatus());
    }

    @Test void financialCeilingUsesExplicitUpperBoundAndUnits() {
        var r=rule();r.setCriterionType(CriterionType.CUSTOM);r.setFieldName("householdIncome");
        r.setOperator(CriterionOperator.LESS_THAN_OR_EQUAL);r.setMinValue(null);r.setMaxValue(new BigDecimal("100000"));r.setStringValue("XOF/YEAR");
        profile.setHouseholdIncome(new BigDecimal("50000"));profile.setIncomeCurrency("XOF");profile.setIncomePeriod("YEAR");
        assertEquals(ELIGIBLE,evaluate(r).getStatus());
        profile.setHouseholdIncome(new BigDecimal("100001"));assertEquals(NOT_ELIGIBLE,evaluate(r).getStatus());
        profile.setIncomeCurrency("EUR");assertEquals(INSUFFICIENT_DATA,evaluate(r).getStatus());
        profile.setIncomeCurrency("XOF");profile.setIncomePeriod("MONTH");assertEquals(INSUFFICIENT_DATA,evaluate(r).getStatus());
    }
    @Test void missingFinancialDataNeverPasses() {
        var r=rule();r.setCriterionType(CriterionType.CUSTOM);r.setFieldName("householdIncome");r.setStringValue("XOF/YEAR");
        assertEquals(INSUFFICIENT_DATA,evaluate(r).getStatus());
    }
    @Test void nationalityAndProgrammeScopeCompareExactValues() {
        var r=rule();r.setCriterionType(CriterionType.CUSTOM);r.setFieldName("nationality");r.setOperator(CriterionOperator.IN_LIST);r.setStringValue("SN;BJ");
        assertEquals(INSUFFICIENT_DATA,evaluate(r).getStatus());profile.setNationality("SN");assertEquals(ELIGIBLE,evaluate(r).getStatus());
        r.setFieldName("programId");r.setStringValue(programme.getId().toString());profile.setTargetProgramId(programme.getId());assertEquals(ELIGIBLE,evaluate(r).getStatus());
        profile.setTargetProgramId(UUID.randomUUID());assertEquals(NOT_ELIGIBLE,evaluate(r).getStatus());
    }

    @Test void publishedMaximumAverageUsesItsUpperBoundWithoutInventingAMinimum() {
        var r=rule();r.setCriterionType(CriterionType.MAX_AVERAGE);r.setOperator(CriterionOperator.LESS_THAN_OR_EQUAL);
        r.setMinValue(null);r.setMaxValue(new BigDecimal("12"));
        assertEquals(ELIGIBLE,evaluate(r).getStatus());
        profile.setGeneralAverage(new BigDecimal("12.000001"));assertEquals(NOT_ELIGIBLE,evaluate(r).getStatus());
    }
}
