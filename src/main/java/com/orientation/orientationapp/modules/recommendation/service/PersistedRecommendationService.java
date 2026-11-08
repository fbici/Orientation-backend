package com.orientation.orientationapp.modules.recommendation.service;

import com.fasterxml.jackson.databind.*;
import com.orientation.orientationapp.modules.recommendation.entity.Recommendation;
import com.orientation.orientationapp.modules.recommendation.repository.RecommendationRepository;
import com.orientation.orientationapp.modules.university.repository.ProgramRepository;
import com.orientation.orientationapp.modules.orientation.repository.AdmissionCriterionRepository;
import com.orientation.orientationapp.modules.orientation.entity.GuideVersion;
import com.orientation.orientationapp.recengine.eligibility.engine.EligibilityEngine;
import com.orientation.orientationapp.recengine.eligibility.model.*;
import com.orientation.orientationapp.recengine.profile.analyzer.impl.DefaultProfileAnalyzer;
import com.orientation.orientationapp.recengine.scoring.engine.impl.RealScoringEngine;
import com.orientation.orientationapp.recengine.ranking.engine.impl.DefaultRankingEngine;
import com.orientation.orientationapp.recengine.scoring.model.CompositeScore;
import com.orientation.orientationapp.recengine.recommendation.dto.request.GenerateRecommendationRequest;
import com.orientation.orientationapp.security.service.ResourceAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.time.*;
import java.math.*;

@Service @RequiredArgsConstructor @Transactional
public class PersistedRecommendationService {
    private final ResourceAccess access;
    private final RecommendationRepository recommendations;
    private final ProgramRepository programmes;
    private final AdmissionCriterionRepository criteria;
    private final DefaultProfileAnalyzer analyzer;
    private final EligibilityEngine eligibility;
    private final RealScoringEngine scoring;
    private final DefaultRankingEngine ranking;
    private final ObjectMapper json;
    private final com.orientation.orientationapp.docintel.document.repository.ExtractedFactRepository sourceFacts;
    private final com.orientation.orientationapp.modules.scholarship.repository.ScholarshipRepository scholarships;
    private final com.orientation.orientationapp.modules.university.service.AcademicCatalogService catalog;
    private final com.orientation.orientationapp.modules.recommendation.repository.RecommendationRunRepository runs;
    public record Item(UUID id,UUID runId,UUID candidateId,UUID programId,UUID guideVersionId,
        int rank,EligibilityStatus eligibilityStatus,BigDecimal score,BigDecimal dataCoverage,
        Instant computedAt,boolean stale,boolean sourceCurrent,JsonNode explanation) {}
    public record Generation(UUID runId,UUID candidateId,Instant computedAt,List<Item> recommendations) {}

    public Generation generate(GenerateRecommendationRequest request) {
        if(request.getBacType()!=null||request.getBacAverage()!=null||request.getSubjectGrades()!=null||
           request.getPreferredFields()!=null||request.getPreferredCountries()!=null||
           request.getLanguage()!=null||request.getBudget()!=null)
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                "Save and confirm academic data before generating; transient grades/preferences are not accepted");
        var candidate=access.candidate(request.getCandidateId()==null?access.ownCandidate().getId():request.getCandidateId());
        if(!candidate.getUser().getId().equals(access.current().getId()))access.require("RECOMMENDATION_EXECUTE");
        var profile=analyzer.analyze(candidate.getId());
        if(profile.getAcademicYearId()==null)throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
            "A confirmed persistent academic profile with an admission year is required");
        var evaluation=evaluate(profile,null);
        var available=evaluation.programmes();var results=evaluation.eligibility();var versions=evaluation.versions();
        var now=Instant.now();
        var computation=new com.orientation.orientationapp.modules.recommendation.entity.RecommendationRun();
        computation.setCandidate(candidate);computation.setCandidateVersion(candidate.getVersion());
        computation.setComputedAt(now);computation.setTotalPrograms(available.size());
        var run=runs.saveAndFlush(computation).getId();var items=new ArrayList<Item>();
        for(var ranked:evaluation.ranked()) {
            var programme=available.stream().filter(p->p.getId().equals(ranked.getProgramId())).findFirst().orElseThrow();
            var outcome=results.get(programme.getId());
            var snapshot=new LinkedHashMap<String,Object>();
            snapshot.put("programName",programme.getName());snapshot.put("sourceUrl",programme.getSourceUrl());
            snapshot.put("academicYearId",profile.getAcademicYearId());
            snapshot.put("eligibility",outcome);snapshot.put("scoring",ranked.getScore());
            var funding=new ArrayList<Map<String,Object>>();
            for(var scholarship:scholarships.findByCountryIdAndAcademicYearId(
                    programme.getFaculty().getCampus().getUniversity().getCountry().getId(),profile.getAcademicYearId())) {
                if(!Boolean.TRUE.equals(scholarship.getVerified()) ||
                        scholarship.getTenantId()!=null&&!scholarship.getTenantId().equals(candidate.getTenant().getId()) ||
                        scholarship.getProgramId()!=null&&!scholarship.getProgramId().equals(programme.getId()) ||
                        scholarship.getUniversityId()!=null&&!scholarship.getUniversityId().equals(programme.getFaculty().getCampus().getUniversity().getId()) ||
                        !Boolean.TRUE.equals(catalog.view(scholarship).get("openForApplications")) ||
                        scholarship.getRemainingSlots()!=null&&scholarship.getRemainingSlots()<=0) continue;
                var fundingItem=new LinkedHashMap<String,Object>();
                fundingItem.put("id",scholarship.getId());fundingItem.put("name",scholarship.getName());
                fundingItem.put("sourceUrl",scholarship.getSourceUrl());fundingItem.put("deadline",scholarship.getDeadline());
                fundingItem.put("amount",scholarship.getAmount());fundingItem.put("currency",scholarship.getCurrency());
                var fundingProfile=profile.toBuilder().targetProgramId(programme.getId())
                    .targetUniversityId(programme.getFaculty().getCampus().getUniversity().getId()).targetStudyField(programme.getStudyField()).build();
                fundingItem.put("eligibility",eligibility.evaluateScholarship(fundingProfile,scholarship.getId()));
                funding.add(fundingItem);
            }
            snapshot.put("scholarships",funding);
            snapshot.put("candidateVersion",candidate.getVersion());snapshot.put("profileSource","SELF_CONFIRMED");
            snapshot.put("admissionProbability",null);
            snapshot.put("scoringVersion","F3.1-1");snapshot.put("eligibilityVersion","F3.1-1");
            var input=new LinkedHashMap<String,Object>();
            input.put("generalAverage",profile.getGeneralAverage());input.put("subjectGrades",profile.getSubjectGrades());
            input.put("gradeScaleId",profile.getGradeScale()==null?null:profile.getGradeScale().getId());
            if(profile.getGradeScale()!=null) {
                var sourceScale=new LinkedHashMap<String,Object>();
                sourceScale.put("min",profile.getGradeScale().getMinScore());sourceScale.put("max",profile.getGradeScale().getMaxScore());
                sourceScale.put("normalizeTo",profile.getGradeScale().getNormalizeTo());input.put("sourceScale",sourceScale);
            }
            input.put("gradesAcademicYearId",profile.getGradesAcademicYearId());input.put("bacType",profile.getBacType());input.put("bacSeries",profile.getBacSeries());
            input.put("diploma",profile.getDiploma());input.put("educationLevel",profile.getEducationLevel());
            input.put("preferredFields",profile.getPreferredFields());input.put("preferredCountries",profile.getPreferredCountries());
            input.put("preferredLanguages",profile.getPreferredLanguages());input.put("language",profile.getLanguage());
            input.put("confirmedAt",candidate.getAcademicConfirmedAt());
            input.put("transcriptAnalysisId",decode(candidate.getAcademicData()).get("transcriptAnalysisId"));
            snapshot.put("academicInput",input);
            var r=Recommendation.builder().candidate(candidate).program(programme)
                .guideVersion(versions.get(programme.getId())).runId(run).candidateVersion(candidate.getVersion())
                .eligibilityStatus(outcome.getStatus()).resultPayload(encode(snapshot))
                .eligible(outcome.isEligible()).rank(ranked.getRank()).recommendedAt(now)
                .matchScore(ranked.getScore().getFinalScore()==null?null:ranked.getScore().getFinalScore().setScale(2,RoundingMode.HALF_UP))
                .confidenceScore(ranked.getScore().getConfidence().setScale(2,RoundingMode.HALF_UP)).build();
            items.add(view(recommendations.saveAndFlush(r)));
        }
        return new Generation(run,candidate.getId(),now,items);
    }
    public record Evaluation(
        List<com.orientation.orientationapp.modules.university.entity.Program> programmes,
        List<com.orientation.orientationapp.recengine.ranking.model.RankedRecommendation> ranked,
        Map<UUID,EligibilityResult> eligibility,Map<UUID,GuideVersion> versions) {}
    /** Shared, read-only F3.1 orchestration. It never persists a recommendation or modifies a profile. */
    @Transactional(readOnly=true) public Evaluation evaluate(
        com.orientation.orientationapp.recengine.profile.model.AcademicProfile profile,List<UUID> selectedIds) {
        var candidate=access.candidate(profile.getCandidateId());
        if(!Objects.equals(profile.getTenantId(),candidate.getTenant().getId()))throw ResourceAccess.denied();
        var available=programmes.findPublishedAvailable(candidate.getTenant().getId()).stream()
            .filter(p->selectedIds!=null?selectedIds.contains(p.getId()):
                p.getAcademicYearId()==null||p.getAcademicYearId().equals(profile.getAcademicYearId()))
            .sorted(Comparator.comparing(p->p.getId().toString())).toList();
        var scores=new ArrayList<CompositeScore>();var contexts=new ArrayList<Map<String,Object>>();
        var results=new LinkedHashMap<UUID,EligibilityResult>();var versions=new HashMap<UUID,GuideVersion>();
        for(var programme:available) {
            var country=programme.getFaculty().getCampus().getUniversity().getCountry().getId();
            var options=criteria.findByProgramId(programme.getId()).stream().map(r->r.getGuideVersion()).distinct()
                .filter(v->v!=null&&v.getStatus()==com.orientation.orientationapp.common.enums.GuideVersionStatus.ACTIVE)
                .filter(v->Boolean.TRUE.equals(v.getActive())&&v.getEffectiveDate()!=null&&!v.getEffectiveDate().isAfter(LocalDate.now()))
                .filter(v->v.getExpiryDate()==null||!v.getExpiryDate().isBefore(LocalDate.now()))
                .filter(v->v.getOrientationGuide()!=null&&Boolean.TRUE.equals(v.getOrientationGuide().getActive()))
                .filter(v->v.getOrientationGuide().getAcademicYear().getId().equals(profile.getAcademicYearId()))
                .filter(v->v.getOrientationGuide().getCountry().getId().equals(country))
                .filter(v->v.getOrientationGuide().getTenantId()==null||
                    v.getOrientationGuide().getTenantId().equals(candidate.getTenant().getId()))
                .toList();
            // More than one applicable version is ambiguous; do not choose arbitrarily.
            var version=options.size()==1?options.getFirst():null;
            var outcome=eligibility.evaluateProgram(profile,programme.getId(),version==null?null:version.getId());
            results.put(programme.getId(),outcome);
            if(version!=null)versions.put(programme.getId(),version);
            scores.add(scoring.computeScore(profile,programme.getId(),version==null?null:version.getId()));
            contexts.add(Map.of("id",programme.getId(),"name",programme.getName(),
                "university",programme.getFaculty().getCampus().getUniversity().getName(),
                "faculty",programme.getFaculty().getName(),"eligibility",outcome.getStatus()));
        }
        if(selectedIds!=null && available.size()!=selectedIds.size())throw ResourceAccess.denied();
        return new Evaluation(available,ranking.rank(scores,contexts),results,versions);
    }
    @Transactional(readOnly=true) public Page<Item> list(UUID candidateId,UUID runId,int page,int size) {
        var candidate=access.candidate(candidateId==null?access.ownCandidate().getId():candidateId);
        readPermission(candidate);
        var pageable=PageRequest.of(Math.max(0,page),Math.max(1,Math.min(size,100)));
        return (runId==null?recommendations.findByCandidateIdAndRunIdIsNotNullOrderByRecommendedAtDescRankAsc(candidate.getId(),pageable)
            :recommendations.findByCandidateIdAndRunIdOrderByRankAsc(candidate.getId(),runId,pageable)).map(this::view);
    }
    @Transactional(readOnly=true) public Item get(UUID id) {
        var r=recommendations.findById(id).filter(x->x.getRunId()!=null).orElseThrow(ResourceAccess::denied);
        readPermission(access.candidate(r.getCandidate().getId()));return view(r);
    }
    public record RunView(UUID runId,UUID candidateId,Long candidateVersion,Instant computedAt,int totalPrograms,boolean stale) {}
    @Transactional(readOnly=true) public Page<RunView> history(UUID candidateId,int page,int size) {
        var c=access.candidate(candidateId==null?access.ownCandidate().getId():candidateId);
        readPermission(c);
        return runs.findByCandidateIdOrderByComputedAtDesc(c.getId(),PageRequest.of(Math.max(0,page),Math.max(1,Math.min(100,size))))
            .map(r->new RunView(r.getId(),c.getId(),r.getCandidateVersion(),r.getComputedAt(),r.getTotalPrograms(),
                !Objects.equals(r.getCandidateVersion(),c.getVersion())));
    }
    private void readPermission(com.orientation.orientationapp.modules.user.entity.Candidate c) {
        if(!c.getUser().getId().equals(access.current().getId()))access.require("RECOMMENDATION_READ");
    }
    private Item view(Recommendation r) {
        return new Item(r.getId(),r.getRunId(),r.getCandidate().getId(),r.getProgram().getId(),
            r.getGuideVersion()==null?null:r.getGuideVersion().getId(),r.getRank(),r.getEligibilityStatus(),
            r.getMatchScore(),r.getConfidenceScore(),r.getRecommendedAt(),
            !Objects.equals(r.getCandidateVersion(),r.getCandidate().getVersion()),sourceCurrent(r),decode(r.getResultPayload()));
    }
    private boolean sourceCurrent(Recommendation r) {
        var version=r.getGuideVersion();var today=LocalDate.now();
        boolean current=version!=null && version.getStatus()==com.orientation.orientationapp.common.enums.GuideVersionStatus.ACTIVE &&
            Boolean.TRUE.equals(version.getActive()) && version.getEffectiveDate()!=null && !version.getEffectiveDate().isAfter(today) &&
            (version.getExpiryDate()==null||!version.getExpiryDate().isBefore(today)) &&
            Boolean.TRUE.equals(version.getOrientationGuide().getActive()) &&
            Boolean.TRUE.equals(r.getProgram().getVerified()) && Boolean.TRUE.equals(r.getProgram().getAvailable());
        if(!current)return false;
        if(version.getUpdatedAt()!=null&&version.getUpdatedAt().isAfter(r.getRecommendedAt()) ||
           r.getProgram().getUpdatedAt()!=null&&r.getProgram().getUpdatedAt().isAfter(r.getRecommendedAt()))return false;
        for(var criterion:decode(r.getResultPayload()).path("eligibility").path("criteria")) {
            if(criterion.path("sourceFactId").isMissingNode()||criterion.path("sourceFactId").isNull())return false;
            var fact=sourceFacts.findById(UUID.fromString(criterion.path("sourceFactId").asText())).orElse(null);
            if(fact==null||!"VALIDATED".equals(fact.getStatus())||fact.getPublishedAt()==null||
                !Objects.equals(fact.getGuideVersionId(),version.getId())||fact.getDocumentVersion()==null||
                !Boolean.TRUE.equals(fact.getDocumentVersion().getActive())||
                fact.getUpdatedAt()!=null&&fact.getUpdatedAt().isAfter(r.getRecommendedAt()))return false;
        }
        return true;
    }
    private String encode(Object data){try{return json.writeValueAsString(data);}catch(Exception e){throw new IllegalStateException(e);}}
    private JsonNode decode(String data){try{return json.readTree(data);}catch(Exception e){throw new IllegalStateException(e);}}
}
