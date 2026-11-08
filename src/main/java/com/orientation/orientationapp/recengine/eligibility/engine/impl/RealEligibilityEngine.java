package com.orientation.orientationapp.recengine.eligibility.engine.impl;

import com.orientation.orientationapp.common.enums.*;
import com.orientation.orientationapp.modules.orientation.entity.*;
import com.orientation.orientationapp.modules.orientation.repository.AdmissionCriterionRepository;
import com.orientation.orientationapp.modules.scholarship.repository.ScholarshipCriterionRepository;
import com.orientation.orientationapp.recengine.eligibility.engine.EligibilityEngine;
import com.orientation.orientationapp.recengine.eligibility.model.*;
import com.orientation.orientationapp.recengine.profile.model.AcademicProfile;
import com.orientation.orientationapp.recengine.profile.normalization.GradeNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.*;
import static com.orientation.orientationapp.recengine.eligibility.model.EligibilityStatus.*;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class RealEligibilityEngine implements EligibilityEngine {
    private final AdmissionCriterionRepository admissionCriterionRepository;
    private final ScholarshipCriterionRepository scholarshipCriterionRepository;
    private final com.orientation.orientationapp.modules.orientation.repository.GradeScaleRepository scales;
    private final GradeNormalizer normalizer;
    private final com.orientation.orientationapp.docintel.document.repository.ExtractedFactRepository facts;

    @Override public EligibilityResult evaluateProgram(AcademicProfile profile, UUID programId, UUID guideVersionId) {
        if (profile == null || programId == null || guideVersionId == null)
            return result(List.of(), "Explicit profile, programme and guide version are required");
        return evaluateCriteria(profile, admissionCriterionRepository.findByGuideVersionIdAndProgramId(guideVersionId, programId));
    }

    @Override public EligibilityResult evaluateFaculty(AcademicProfile profile, UUID facultyId) {
        // This legacy signature has no applicable guide version. Never select one implicitly.
        return result(List.of(), "A faculty cannot be evaluated without an explicit guide version");
    }

    @Override public EligibilityResult evaluateScholarship(AcademicProfile profile, UUID scholarshipId) {
        if (profile == null || scholarshipId == null) return result(List.of(), "Profile and scholarship are required");
        var published = scholarshipCriterionRepository.findByScholarshipId(scholarshipId);
        var applicable=published.stream().map(r->r.getGuideVersion()).filter(Objects::nonNull)
            .filter(v->v.getStatus()==GuideVersionStatus.ACTIVE && Boolean.TRUE.equals(v.getActive()))
            .filter(v->v.getEffectiveDate()!=null&&!v.getEffectiveDate().isAfter(LocalDate.now()))
            .filter(v->v.getExpiryDate()==null||!v.getExpiryDate().isBefore(LocalDate.now()))
            .filter(v->v.getOrientationGuide()!=null&&v.getOrientationGuide().getAcademicYear()!=null&&
                Objects.equals(v.getOrientationGuide().getAcademicYear().getId(),profile.getAcademicYearId()))
            .map(v->v.getId()).distinct().toList();
        if(applicable.size()!=1)return result(List.of(),"No unique applicable published scholarship guide");
        var rules = new ArrayList<AdmissionCriterion>();
        for (var rule : published) {
            if(rule.getGuideVersion()==null||!Objects.equals(rule.getGuideVersion().getId(),applicable.getFirst()))continue;
            var scholarship = rule.getScholarship();
            if(scholarship==null)return result(List.of(),"Scholarship is missing");
            var open=com.orientation.orientationapp.modules.scholarship.service.ScholarshipAvailability.open(scholarship,LocalDate.now());
            if(!Boolean.TRUE.equals(open)) {
                var unavailable=result(List.of(),"Scholarship is not known to be open");
                unavailable.setStatus(Boolean.FALSE.equals(open)?NOT_ELIGIBLE:INSUFFICIENT_DATA);return unavailable;
            }
            var admission = AdmissionCriterion.builder().guideVersion(rule.getGuideVersion())
                    .criterionType(rule.getCriterionType()).operator(rule.getOperator())
                    .subject(rule.getSubject()).minValue(rule.getMinValue()).maxValue(rule.getMaxValue())
                    .stringValue(rule.getStringValue()).mandatory(rule.getMandatory())
                    .sourceFactId(rule.getSourceFactId()).fieldName(rule.getFieldName())
                    .valueScaleMax(rule.getValueScaleMax()).logicalGroup(rule.getLogicalGroup()).logicalOperator(rule.getLogicalOperator()).build();
            admission.setId(rule.getId());
            rules.add(admission);
        }
        return evaluateCriteria(profile, rules);
    }

    EligibilityResult evaluateCriteria(AcademicProfile profile, List<AdmissionCriterion> rules) {
        if (profile == null || rules.isEmpty()) return result(List.of(), "No applicable published criteria");
        var evaluated = new ArrayList<EligibilityResult.EligibilityCriterion>();
        for (var rule : rules) {
            String invalid = contextError(profile, rule);
            var state = invalid == null ? evaluate(profile, rule) : INSUFFICIENT_DATA;
            var source=invalid==null?facts.findById(rule.getSourceFactId()).orElse(null):null;
            evaluated.add(EligibilityResult.EligibilityCriterion.builder()
                    .sourceDocumentId(source==null||source.getDocument()==null?null:source.getDocument().getId())
                    .sourceDocumentVersionId(source==null||source.getDocumentVersion()==null?null:source.getDocumentVersion().getId())
                    .sourcePage(source==null?null:source.getPageNumber())
                    .sourcePassage(source==null?null:source.getSourcePassage())
                    .criterionId(rule.getId()).sourceFactId(rule.getSourceFactId())
                    .guideVersionId(rule.getGuideVersion() == null ? null : rule.getGuideVersion().getId())
                    .academicYearId(rule.getGuideVersion() == null || rule.getGuideVersion().getOrientationGuide() == null || rule.getGuideVersion().getOrientationGuide().getAcademicYear() == null
                            ? null : rule.getGuideVersion().getOrientationGuide().getAcademicYear().getId())
                    .logicalGroup(rule.getLogicalGroup()).logicalOperator(Objects.toString(rule.getLogicalOperator(),null)).mandatory(rule.getMandatory()).criterionName(String.valueOf(rule.getCriterionType()))
                    .expectedValue(expected(rule)).actualValue(actualText(profile, rule))
                    .normalization(invalid==null?normalizationTrace(profile,rule):Map.of())
                    .status(state).met(state == ELIGIBLE)
                    .message(invalid != null ? invalid : state == INSUFFICIENT_DATA
                            ? "Missing data, scale or unsupported rule" : state == ELIGIBLE
                            ? "Published criterion satisfied" : "Published criterion not satisfied").build());
        }
        // Mandatory groups combine with AND; each group uses its explicitly published operator.
        var groups = new LinkedHashMap<String,List<Integer>>();
        for (int i=0;i<rules.size();i++) if (Boolean.TRUE.equals(rules.get(i).getMandatory()))
            groups.computeIfAbsent(rules.get(i).getLogicalGroup(), k -> new ArrayList<>()).add(i);
        var groupResults = new ArrayList<EligibilityStatus>();
        for (var indexes : groups.values()) {
            var ops = new HashSet<LogicalOperator>();
            indexes.forEach(i -> ops.add(rules.get(i).getLogicalOperator()));
            if (ops.size() != 1 || ops.contains(null)) groupResults.add(INSUFFICIENT_DATA);
            else groupResults.add(combine(indexes.stream().map(i -> evaluated.get(i).getStatus()).toList(),
                    ops.iterator().next() == LogicalOperator.OR));
        }
        EligibilityStatus state = groups.isEmpty() ? INSUFFICIENT_DATA : combine(groupResults, false);
        var output = result(evaluated, null);
        output.setStatus(state); output.setEligible(state == ELIGIBLE);
        if (groups.isEmpty()) output.getWarnings().add("No mandatory admission conditions published");
        return output;
    }

    public static EligibilityStatus combine(List<EligibilityStatus> states, boolean or) {
        if (states.isEmpty()) return INSUFFICIENT_DATA;
        if (or && states.contains(ELIGIBLE)) return ELIGIBLE;
        if (!or && states.contains(NOT_ELIGIBLE)) return NOT_ELIGIBLE;
        if (states.contains(INSUFFICIENT_DATA)) return INSUFFICIENT_DATA;
        return or ? NOT_ELIGIBLE : ELIGIBLE;
    }

    private String contextError(AcademicProfile p, AdmissionCriterion r) {
        var v = r.getGuideVersion(); var today = LocalDate.now();
        if (v == null || v.getStatus() != GuideVersionStatus.ACTIVE || !Boolean.TRUE.equals(v.getActive())
                || Boolean.TRUE.equals(v.getDeleted()) || v.getEffectiveDate() == null ||
                v.getEffectiveDate().isAfter(today) || v.getExpiryDate() != null && v.getExpiryDate().isBefore(today))
            return "Guide version is not currently applicable";
        var g = v.getOrientationGuide();
        if (g == null || g.getCountry()==null || !Boolean.TRUE.equals(g.getActive()) || Boolean.TRUE.equals(g.getDeleted()) ||
                p.getAcademicYearId() == null || g.getAcademicYear() == null ||
                !p.getAcademicYearId().equals(g.getAcademicYear().getId()))
            return "Academic year is missing or different";
        if (g.getTenantId() != null && !g.getTenantId().equals(p.getTenantId()))
            return "Guide belongs to another tenant";
        if (r.getSourceFactId() == null) return "Validated source reference is missing";
        var fact=facts.findById(r.getSourceFactId()).orElse(null);
        if(fact==null || !"VALIDATED".equals(fact.getStatus()) || fact.getPublishedAt()==null ||
                !Objects.equals(fact.getPublishedEntityId(),r.getId()) ||
                !Objects.equals(fact.getGuideVersionId(),v.getId()) ||
                fact.getDocumentVersion()==null || !Boolean.TRUE.equals(fact.getDocumentVersion().getActive()))
            return "Source fact is not validated, published, or active";
        if (r.getProgram() != null) {
            var programme = r.getProgram();
            if (!Boolean.TRUE.equals(programme.getVerified()) || !Boolean.TRUE.equals(programme.getAvailable()) ||
                    programme.getFaculty() == null || programme.getFaculty().getCampus() == null)
                return "Programme is unverified or unavailable";
            var university = programme.getFaculty().getCampus().getUniversity();
            if (university == null || university.getCountry() == null || g.getCountry() == null ||
                    !university.getCountry().getId().equals(g.getCountry().getId()) ||
                    !Boolean.TRUE.equals(university.getVerified()) ||
                    !Boolean.TRUE.equals(programme.getFaculty().getVerified()) ||
                    !Boolean.TRUE.equals(programme.getFaculty().getCampus().getVerified()) ||
                    !Boolean.TRUE.equals(university.getActive()) || university.getStatus()!=UniversityStatus.ACTIVE ||
                    !Boolean.TRUE.equals(programme.getFaculty().getActive()) ||
                    !Boolean.TRUE.equals(programme.getFaculty().getCampus().getActive()))
                return "Programme hierarchy or source country is not verified";
            if (university.getTenantId() != null && !university.getTenantId().equals(p.getTenantId()))
                return "Programme belongs to another tenant";
            if (programme.getAcademicYearId() != null && !programme.getAcademicYearId().equals(p.getAcademicYearId()))
                return "Programme belongs to another academic year";
        }
        return null;
    }

    private EligibilityStatus evaluate(AcademicProfile p, AdmissionCriterion r) {
        if (r.getCriterionType() == null || r.getOperator() == null) return INSUFFICIENT_DATA;
        String field = r.getFieldName();
        if (r.getCriterionType() == CriterionType.CUSTOM) {
            if("householdIncome".equals(field)) {
                String unit=p.getIncomeCurrency()==null||p.getIncomePeriod()==null?null:p.getIncomeCurrency()+"/"+p.getIncomePeriod();
                if(unit==null||!unit.equals(r.getStringValue())||p.getHouseholdIncome()==null)return INSUFFICIENT_DATA;
                return numeric(p.getHouseholdIncome(),r);
            }
            String text = switch (field == null ? "" : field) {
                case "bacType" -> p.getBacType();
                case "bacSeries" -> p.getBacSeries();
                case "diploma" -> p.getDiploma();
                case "educationLevel" -> p.getEducationLevel();
                case "language" -> p.getLanguage();
                case "country", "countryId" -> p.getCountry();
                case "nationality", "nationalityCountryId" -> p.getNationality();
                case "programId" -> Objects.toString(p.getTargetProgramId(),null);
                case "universityId" -> Objects.toString(p.getTargetUniversityId(),null);
                case "studyField" -> p.getTargetStudyField();
                default -> null;
            };
            return compareText(text, r);
        }
        BigDecimal raw; GradeScale source = p.getGradeScale(), target;
        switch (r.getCriterionType()) {
            case MIN_AVERAGE, MAX_AVERAGE -> {
                raw = p.getGeneralAverage();
                var guide = r.getGuideVersion().getOrientationGuide();
                target = scales.findByCountryIdAndAcademicYearId(guide.getCountry().getId(),
                        guide.getAcademicYear().getId()).orElse(null);
            }
            case MIN_GRADE, MAX_GRADE, SPECIFIC_GRADE -> {
                if (r.getSubject() == null || r.getSubject().getCode() == null || p.getSubjectGrades() == null)
                    return INSUFFICIENT_DATA;
                String code = r.getSubject().getCode();
                raw = p.getSubjectGrades().get(code);
                if (p.getSubjectGradeScales() != null) source = p.getSubjectGradeScales().get(code);
                target = r.getSubject().getGradeScale();
            }
            default -> {return INSUFFICIENT_DATA;}
        }
        var guide=r.getGuideVersion().getOrientationGuide();
        if (source == null || target == null || source.getAcademicYear() == null ||
                target.getCountry()==null || !target.getCountry().getId().equals(guide.getCountry().getId()) ||
                target.getAcademicYear()==null || !target.getAcademicYear().getId().equals(p.getAcademicYearId()) ||
                p.getGradesAcademicYearId() == null ||
                !p.getGradesAcademicYearId().equals(source.getAcademicYear().getId()) ||
                r.getValueScaleMax() == null || target.getMaxScore() == null ||
                r.getValueScaleMax().compareTo(target.getMaxScore()) != 0)
            return INSUFFICIENT_DATA;
        BigDecimal bound=switch(r.getOperator()) {
            case LESS_THAN,LESS_THAN_OR_EQUAL -> r.getMaxValue()!=null?r.getMaxValue():r.getMinValue();
            default -> r.getMinValue();
        };
        var min = normalizer.compare(raw, source, bound, target);
        if (min.isEmpty()) return INSUFFICIENT_DATA;
        int comparison = min.get();
        boolean met;
        switch (r.getOperator()) {
            case EQUAL -> met = comparison == 0;
            case NOT_EQUAL -> met = comparison != 0;
            case GREATER_THAN -> met = comparison > 0;
            case GREATER_THAN_OR_EQUAL -> met = comparison >= 0;
            case LESS_THAN -> met = comparison < 0;
            case LESS_THAN_OR_EQUAL -> met = comparison <= 0;
            case BETWEEN -> {
                var max = normalizer.compare(raw, source, r.getMaxValue(), target);
                if (max.isEmpty() || r.getMaxValue().compareTo(r.getMinValue()) < 0) return INSUFFICIENT_DATA;
                met = comparison >= 0 && max.get() <= 0;
            }
            default -> {return INSUFFICIENT_DATA;}
        }
        return met ? ELIGIBLE : NOT_ELIGIBLE;
    }

    private EligibilityStatus numeric(BigDecimal actual,AdmissionCriterion rule) {
        if(actual==null||rule.getOperator()==null)return INSUFFICIENT_DATA;
        BigDecimal bound=switch(rule.getOperator()) {
            case LESS_THAN,LESS_THAN_OR_EQUAL -> rule.getMaxValue()!=null?rule.getMaxValue():rule.getMinValue();
            default -> rule.getMinValue();
        };
        if(bound==null)return INSUFFICIENT_DATA;
        int compared=actual.compareTo(bound);boolean met;
        switch(rule.getOperator()) {
            case EQUAL -> met=compared==0;
            case NOT_EQUAL -> met=compared!=0;
            case GREATER_THAN -> met=compared>0;
            case GREATER_THAN_OR_EQUAL -> met=compared>=0;
            case LESS_THAN -> met=compared<0;
            case LESS_THAN_OR_EQUAL -> met=compared<=0;
            case BETWEEN -> {
                if(rule.getMaxValue()==null||rule.getMaxValue().compareTo(rule.getMinValue())<0)return INSUFFICIENT_DATA;
                met=compared>=0&&actual.compareTo(rule.getMaxValue())<=0;
            }
            default -> {return INSUFFICIENT_DATA;}
        }
        return met?ELIGIBLE:NOT_ELIGIBLE;
    }
    private EligibilityStatus compareText(String actual, AdmissionCriterion r) {
        if (actual == null || actual.isBlank() || r.getStringValue() == null || r.getStringValue().isBlank())
            return INSUFFICIENT_DATA;
        // Lists are explicitly delimited; do not infer membership from substrings.
        var values = Arrays.stream(r.getStringValue().split("[,;|]")).map(String::trim)
                .filter(s -> !s.isEmpty()).toList();
        boolean included = values.stream().anyMatch(s -> s.equalsIgnoreCase(actual.trim()));
        return switch (r.getOperator()) {
            case IN_LIST -> included ? ELIGIBLE : NOT_ELIGIBLE;
            case NOT_IN_LIST -> included ? NOT_ELIGIBLE : ELIGIBLE;
            case EQUAL -> values.size() != 1 ? INSUFFICIENT_DATA : included ? ELIGIBLE : NOT_ELIGIBLE;
            case NOT_EQUAL -> values.size() != 1 ? INSUFFICIENT_DATA : included ? NOT_ELIGIBLE : ELIGIBLE;
            default -> INSUFFICIENT_DATA;
        };
    }
    private Map<String,Object> normalizationTrace(AcademicProfile p,AdmissionCriterion r) {
        if(r.getCriterionType()!=CriterionType.MIN_AVERAGE && r.getCriterionType()!=CriterionType.MAX_AVERAGE &&
           r.getCriterionType()!=CriterionType.MIN_GRADE && r.getCriterionType()!=CriterionType.MAX_GRADE &&
           r.getCriterionType()!=CriterionType.SPECIFIC_GRADE)return Map.of();
        GradeScale source=p.getGradeScale(),target=null;BigDecimal value=p.getGeneralAverage();
        if(r.getSubject()!=null) {
            String code=r.getSubject().getCode();
            value=p.getSubjectGrades()==null||code==null?null:p.getSubjectGrades().get(code);
            if(p.getSubjectGradeScales()!=null && code!=null)source=p.getSubjectGradeScales().get(code);
            target=r.getSubject().getGradeScale();
        } else if(r.getCriterionType()==CriterionType.MIN_AVERAGE||r.getCriterionType()==CriterionType.MAX_AVERAGE) {
            var guide=r.getGuideVersion().getOrientationGuide();
            target=scales.findByCountryIdAndAcademicYearId(guide.getCountry().getId(),guide.getAcademicYear().getId()).orElse(null);
        }
        var trace=new LinkedHashMap<String,Object>();trace.put("originalValue",value);
        trace.put("sourceScale",scaleTrace(source));trace.put("targetScale",scaleTrace(target));
        trace.put("convertedValue",normalizer.convert(value,source,target).orElse(null));
        trace.put("comparisonMethod","EXACT_CROSS_MULTIPLICATION");
        trace.put("displayRoundingApplied",false);
        return trace;
    }
    private Map<String,Object> scaleTrace(GradeScale scale) {
        var trace=new LinkedHashMap<String,Object>();if(scale==null)return trace;
        trace.put("id",scale.getId());trace.put("min",scale.getMinScore());trace.put("max",scale.getMaxScore());
        trace.put("normalizeTo",scale.getNormalizeTo());
        trace.put("countryId",scale.getCountry()==null?null:scale.getCountry().getId());
        trace.put("academicYearId",scale.getAcademicYear()==null?null:scale.getAcademicYear().getId());
        return trace;
    }
    private String actualText(AcademicProfile p, AdmissionCriterion r) {
        if("householdIncome".equals(r.getFieldName()))return p.getHouseholdIncome()==null?null:
            p.getHouseholdIncome()+" "+p.getIncomeCurrency()+"/"+p.getIncomePeriod();
        if (r.getSubject() != null && r.getSubject().getCode()!=null && p.getSubjectGrades() != null)
            return Objects.toString(p.getSubjectGrades().get(r.getSubject().getCode()), null);
        if (r.getCriterionType() == CriterionType.MIN_AVERAGE || r.getCriterionType() == CriterionType.MAX_AVERAGE)
            return Objects.toString(p.getGeneralAverage(), null);
        return switch (Objects.toString(r.getFieldName(), "")) {
            case "bacType" -> p.getBacType();
                case "bacSeries" -> p.getBacSeries();
            case "diploma" -> p.getDiploma();
            case "educationLevel" -> p.getEducationLevel();
            case "language" -> p.getLanguage();
            case "country", "countryId" -> p.getCountry();
                case "nationality", "nationalityCountryId" -> p.getNationality();
                case "programId" -> Objects.toString(p.getTargetProgramId(),null);
                case "universityId" -> Objects.toString(p.getTargetUniversityId(),null);
                case "studyField" -> p.getTargetStudyField();
            default -> null;
        };
    }
    private String expected(AdmissionCriterion r) {
        if("householdIncome".equals(r.getFieldName()))
            return r.getOperator()+" "+(r.getMinValue()==null?r.getMaxValue():r.getMinValue())+(r.getMinValue()!=null&&r.getMaxValue()!=null?".."+r.getMaxValue():"")+" "+r.getStringValue();
        return r.getOperator() + " " + (r.getStringValue() != null ? r.getStringValue() :
                r.getMinValue() + (r.getMaxValue() == null ? "" : ".." + r.getMaxValue()));
    }
    private EligibilityResult result(List<EligibilityResult.EligibilityCriterion> criteria, String warning) {
        var warnings = new ArrayList<String>(); var blocking = new ArrayList<String>();
        if (warning != null) warnings.add(warning);
        criteria.forEach(c -> {if(c.getStatus() == INSUFFICIENT_DATA) warnings.add(c.getMessage());
            if(c.getStatus() == NOT_ELIGIBLE) {
                if(Boolean.TRUE.equals(c.getMandatory()))blocking.add(c.getCriterionName());
                else warnings.add("Optional criterion not satisfied: "+c.getCriterionName());
            }});
        // Score is withheld when at least one criterion is unknown.
        BigDecimal score = criteria.isEmpty() || criteria.stream().anyMatch(c -> c.getStatus() == INSUFFICIENT_DATA)
                ? null : BigDecimal.valueOf(criteria.stream().filter(c -> c.isMet()).count())
                .multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(criteria.size()), MathContext.DECIMAL128);
        return EligibilityResult.builder().status(INSUFFICIENT_DATA).eligible(false).conditionallyEligible(false)
                .eligibilityScore(score).criteria(criteria).warnings(warnings).blockingIssues(blocking).build();
    }
}
