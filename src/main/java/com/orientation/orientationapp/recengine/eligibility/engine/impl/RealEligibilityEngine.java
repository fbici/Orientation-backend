package com.orientation.orientationapp.recengine.eligibility.engine.impl;

import com.orientation.orientationapp.common.enums.CriterionOperator;
import com.orientation.orientationapp.common.enums.CriterionType;
import com.orientation.orientationapp.modules.orientation.entity.AdmissionCriterion;
import com.orientation.orientationapp.modules.orientation.repository.AdmissionCriterionRepository;
import com.orientation.orientationapp.modules.scholarship.entity.ScholarshipCriterion;
import com.orientation.orientationapp.modules.scholarship.repository.ScholarshipCriterionRepository;
import com.orientation.orientationapp.recengine.eligibility.engine.EligibilityEngine;
import com.orientation.orientationapp.recengine.eligibility.model.EligibilityResult;
import com.orientation.orientationapp.recengine.profile.model.AcademicProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealEligibilityEngine implements EligibilityEngine {

    private final AdmissionCriterionRepository admissionCriterionRepository;
    private final ScholarshipCriterionRepository scholarshipCriterionRepository;

    @Override
    public EligibilityResult evaluateProgram(AcademicProfile profile, UUID programId, UUID guideVersionId) {
        log.info("Evaluating eligibility for program: {} with guide: {}", programId, guideVersionId);

        List<AdmissionCriterion> criteria = admissionCriterionRepository
                .findByGuideVersionIdAndProgramId(guideVersionId, programId);

        return evaluateCriteria(profile, criteria);
    }

    @Override
    public EligibilityResult evaluateScholarship(AcademicProfile profile, UUID scholarshipId) {
        log.info("Evaluating eligibility for scholarship: {}", scholarshipId);

        List<ScholarshipCriterion> criteria = scholarshipCriterionRepository
                .findByScholarshipId(scholarshipId);

        List<EligibilityResult.EligibilityCriterion> evaluatedCriteria = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> blockingIssues = new ArrayList<>();
        boolean allMandatoryMet = true;

        for (ScholarshipCriterion criterion : criteria) {
            boolean met = evaluateSingleCriterion(profile, criterion.getCriterionType().name(),
                    criterion.getOperator().name(), criterion.getMinValue(), criterion.getMaxValue(),
                    criterion.getStringValue());

            EligibilityResult.EligibilityCriterion evalCriterion = EligibilityResult.EligibilityCriterion.builder()
                    .criterionName(criterion.getCriterionType().name())
                    .expectedValue(formatExpectedValue(criterion))
                    .actualValue(getActualValue(profile, criterion.getCriterionType().name()))
                    .met(met)
                    .message(met ? "Critère rempli" : "Critère non rempli")
                    .build();

            evaluatedCriteria.add(evalCriterion);

            if (!met && criterion.getMandatory()) {
                allMandatoryMet = false;
                blockingIssues.add("Critère obligatoire non rempli: " + criterion.getCriterionType().name());
            } else if (!met) {
                warnings.add("Critère optionnel non rempli: " + criterion.getCriterionType().name());
            }
        }

        long metCount = evaluatedCriteria.stream().filter(EligibilityResult.EligibilityCriterion::isMet).count();
        BigDecimal score = criteria.isEmpty() ? BigDecimal.ZERO :
                BigDecimal.valueOf((double) metCount / criteria.size() * 100);

        return EligibilityResult.builder()
                .eligible(allMandatoryMet && !evaluatedCriteria.isEmpty())
                .conditionallyEligible(!evaluatedCriteria.isEmpty() && !blockingIssues.isEmpty())
                .eligibilityScore(score)
                .criteria(evaluatedCriteria)
                .warnings(warnings)
                .blockingIssues(blockingIssues)
                .build();
    }

    @Override
    public EligibilityResult evaluateFaculty(AcademicProfile profile, UUID facultyId) {
        log.info("Evaluating eligibility for faculty: {}", facultyId);

        List<AdmissionCriterion> criteria = admissionCriterionRepository
                .findByGuideVersionIdAndFacultyId(null, facultyId);

        return evaluateCriteria(profile, criteria);
    }

    private EligibilityResult evaluateCriteria(AcademicProfile profile, List<AdmissionCriterion> criteria) {
        List<EligibilityResult.EligibilityCriterion> evaluatedCriteria = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> blockingIssues = new ArrayList<>();
        boolean allMandatoryMet = true;

        for (AdmissionCriterion criterion : criteria) {
            boolean met = evaluateSingleCriterion(profile, criterion.getCriterionType().name(),
                    criterion.getOperator().name(), criterion.getMinValue(), criterion.getMaxValue(),
                    criterion.getStringValue());

            EligibilityResult.EligibilityCriterion evalCriterion = EligibilityResult.EligibilityCriterion.builder()
                    .criterionName(criterion.getCriterionType().name())
                    .expectedValue(formatExpectedValue(criterion))
                    .actualValue(getActualValue(profile, criterion.getCriterionType().name()))
                    .met(met)
                    .message(met ? "Critère rempli" : "Critère non rempli")
                    .build();

            evaluatedCriteria.add(evalCriterion);

            if (!met && criterion.getMandatory()) {
                allMandatoryMet = false;
                blockingIssues.add("Critère obligatoire non rempli: " + criterion.getCriterionType().name());
            } else if (!met) {
                warnings.add("Critère optionnel non rempli: " + criterion.getCriterionType().name());
            }
        }

        long metCount = evaluatedCriteria.stream().filter(EligibilityResult.EligibilityCriterion::isMet).count();
        BigDecimal score = criteria.isEmpty() ? BigDecimal.ZERO :
                BigDecimal.valueOf((double) metCount / criteria.size() * 100);

        return EligibilityResult.builder()
                .eligible(allMandatoryMet && !evaluatedCriteria.isEmpty())
                .conditionallyEligible(!evaluatedCriteria.isEmpty() && !blockingIssues.isEmpty())
                .eligibilityScore(score)
                .criteria(evaluatedCriteria)
                .warnings(warnings)
                .blockingIssues(blockingIssues)
                .build();
    }

    private boolean evaluateSingleCriterion(AcademicProfile profile, String criterionType,
                                             String operator, BigDecimal minValue,
                                             BigDecimal maxValue, String stringValue) {
        BigDecimal actualValue = getActualBigDecimalValue(profile, criterionType);
        CriterionOperator op = CriterionOperator.valueOf(operator);

        if (actualValue == null) return false;

        return switch (op) {
            case EQUAL -> minValue != null && actualValue.compareTo(minValue) == 0;
            case NOT_EQUAL -> minValue != null && actualValue.compareTo(minValue) != 0;
            case GREATER_THAN -> minValue != null && actualValue.compareTo(minValue) > 0;
            case GREATER_THAN_OR_EQUAL -> minValue != null && actualValue.compareTo(minValue) >= 0;
            case LESS_THAN -> minValue != null && actualValue.compareTo(minValue) < 0;
            case LESS_THAN_OR_EQUAL -> minValue != null && actualValue.compareTo(minValue) <= 0;
            case BETWEEN -> minValue != null && maxValue != null &&
                    actualValue.compareTo(minValue) >= 0 && actualValue.compareTo(maxValue) <= 0;
            case IN_LIST, NOT_IN_LIST -> true; // Simplified
        };
    }

    private BigDecimal getActualBigDecimalValue(AcademicProfile profile, String criterionType) {
        return switch (criterionType) {
            case "MIN_GRADE", "MAX_GRADE" -> profile.getBacAverage();
            case "MIN_AVERAGE", "MAX_AVERAGE" -> profile.getGeneralAverage();
            case "SPECIFIC_GRADE" -> profile.getGeneralAverage(); // Would need subject lookup
            default -> profile.getBacAverage();
        };
    }

    private String getActualValue(AcademicProfile profile, String criterionType) {
        BigDecimal value = getActualBigDecimalValue(profile, criterionType);
        return value != null ? value.toString() : "N/A";
    }

    private String formatExpectedValue(AdmissionCriterion criterion) {
        CriterionOperator op = criterion.getOperator();
        return switch (op) {
            case EQUAL -> "= " + criterion.getMinValue();
            case NOT_EQUAL -> "!= " + criterion.getMinValue();
            case GREATER_THAN -> "> " + criterion.getMinValue();
            case GREATER_THAN_OR_EQUAL -> ">= " + criterion.getMinValue();
            case LESS_THAN -> "< " + criterion.getMinValue();
            case LESS_THAN_OR_EQUAL -> "<= " + criterion.getMinValue();
            case BETWEEN -> criterion.getMinValue() + " - " + criterion.getMaxValue();
            case IN_LIST, NOT_IN_LIST -> criterion.getStringValue();
        };
    }

    private String formatExpectedValue(ScholarshipCriterion criterion) {
        CriterionOperator op = criterion.getOperator();
        return switch (op) {
            case EQUAL -> "= " + criterion.getMinValue();
            case NOT_EQUAL -> "!= " + criterion.getMinValue();
            case GREATER_THAN -> "> " + criterion.getMinValue();
            case GREATER_THAN_OR_EQUAL -> ">= " + criterion.getMinValue();
            case LESS_THAN -> "< " + criterion.getMinValue();
            case LESS_THAN_OR_EQUAL -> "<= " + criterion.getMinValue();
            case BETWEEN -> criterion.getMinValue() + " - " + criterion.getMaxValue();
            case IN_LIST, NOT_IN_LIST -> criterion.getStringValue();
        };
    }
}
