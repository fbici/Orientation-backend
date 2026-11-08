package com.orientation.orientationapp.modules.scholarship.service;
import com.orientation.orientationapp.ai.decision.DecisionContext;
import com.orientation.orientationapp.ai.history.DecisionHistoryService;
import com.orientation.orientationapp.modules.scholarship.dto.ScholarshipEvaluationRequest;
import com.orientation.orientationapp.modules.scholarship.entity.Scholarship;
import com.orientation.orientationapp.modules.scholarship.repository.ScholarshipRepository;
import com.orientation.orientationapp.modules.university.service.*;
import com.orientation.orientationapp.recengine.eligibility.engine.EligibilityEngine;
import com.orientation.orientationapp.recengine.eligibility.engine.impl.RealEligibilityEngine;
import com.orientation.orientationapp.recengine.eligibility.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
import java.time.*;
import static com.orientation.orientationapp.recengine.eligibility.model.EligibilityStatus.*;
import static com.orientation.orientationapp.security.service.ResourceAccess.denied;

@Service @RequiredArgsConstructor @Transactional(noRollbackFor=ResponseStatusException.class)
public class ScholarshipRecommendationService {
 private final DecisionContext context;private final DecisionHistoryService history;private final ScholarshipRepository scholarships;
 private final EligibilityEngine eligibility;private final AcademicCatalogService catalog;private final AcademicProvenanceService provenance;
 public DecisionHistoryService.View evaluate(ScholarshipEvaluationRequest request) {
  var owner=context.load(request.candidateId());
  return history.save(owner.candidate(),"SCHOLARSHIP",null,assess(request));
 }
 @Transactional(readOnly=true) public Map<String,Object> assess(ScholarshipEvaluationRequest request) {
  var owner=context.load(request.candidateId());var profile=owner.profile().toBuilder().build();
  if(request.programId()!=null) {
   var programme=context.evaluate(profile,List.of(request.programId())).programmes().getFirst();
   profile.setTargetProgramId(programme.getId());profile.setTargetUniversityId(programme.getFaculty().getCampus().getUniversity().getId());
   profile.setTargetStudyField(programme.getStudyField());
  }
  List<Scholarship> selected;
  if(request.scholarshipIds()!=null) {
   if(request.scholarshipIds().isEmpty()||new HashSet<>(request.scholarshipIds()).size()!=request.scholarshipIds().size())
    throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Select distinct scholarship IDs");
   selected=scholarships.findAllById(request.scholarshipIds());
   if(selected.size()!=request.scholarshipIds().size())throw denied();
  } else {
   var tenant=owner.candidate().getTenant().getId();
   selected=scholarships.findAll((r,q,cb)->cb.and(cb.isTrue(r.get("verified")),
       cb.or(cb.isNull(r.get("tenantId")),cb.equal(r.get("tenantId"),tenant)),
       profile.getAcademicYearId()==null?cb.disjunction():cb.equal(r.get("academicYear").get("id"),profile.getAcademicYearId())),
       PageRequest.of(0,100,Sort.by("id"))).getContent();
  }
  var evaluated=new ArrayList<Map<String,Object>>();var now=LocalDate.now();
  for(var scholarship:selected) {
   if(scholarship.getTenantId()!=null&&!scholarship.getTenantId().equals(owner.candidate().getTenant().getId()))throw denied();
   catalog.get("scholarships",scholarship.getId());
   Boolean open=ScholarshipAvailability.open(scholarship,now);
   var outcome=eligibility.evaluateScholarship(profile,scholarship.getId());
   var scope=ELIGIBLE;var scopeIssues=new ArrayList<String>();
   if(scholarship.getProgramId()!=null) {
    scope=profile.getTargetProgramId()==null?INSUFFICIENT_DATA:scholarship.getProgramId().equals(profile.getTargetProgramId())?ELIGIBLE:NOT_ELIGIBLE;
    if(scope!=ELIGIBLE)scopeIssues.add("Programme cible requis ou different du programme publie de la bourse");
   }
   if(scholarship.getUniversityId()!=null) {
    var university=profile.getTargetUniversityId()==null?INSUFFICIENT_DATA:scholarship.getUniversityId().equals(profile.getTargetUniversityId())?ELIGIBLE:NOT_ELIGIBLE;
    scope=RealEligibilityEngine.combine(List.of(scope,university),false);
    if(university!=ELIGIBLE)scopeIssues.add("Universite cible requise ou differente de celle publiee");
   }
   if(!Objects.equals(profile.getAcademicYearId(),scholarship.getAcademicYear().getId())) {
    scope=INSUFFICIENT_DATA;scopeIssues.add("Annee d'admission manquante ou differente");
   }
   var availability=Boolean.TRUE.equals(open)?ELIGIBLE:Boolean.FALSE.equals(open)?NOT_ELIGIBLE:INSUFFICIENT_DATA;
   var overall=RealEligibilityEngine.combine(List.of(scope,availability,outcome.getStatus()),false);
   var item=new LinkedHashMap<String,Object>(catalog.view(scholarship));
   item.put("scholarshipId",scholarship.getId());item.put("openForApplications",open);
   item.put("availability",!Boolean.TRUE.equals(scholarship.getVerified())?"UNVERIFIED":open==null?"UNKNOWN":open?"OPEN":"CLOSED");
   item.put("eligibilityStatus",overall);item.put("eligibility",outcome);item.put("scopeIssues",scopeIssues);
   item.put("evaluatedAt",Instant.now());item.put("sources",Boolean.TRUE.equals(scholarship.getVerified())?
       provenance.sources("scholarships",scholarship.getId(),null,true):List.of());
   item.put("disclaimer","Admissibilite indicative selon les conditions connues ; aucune attribution garantie.");
   evaluated.add(item);
  }
  var payload=new LinkedHashMap<String,Object>();payload.put("type","SCHOLARSHIP");payload.put("scholarships",evaluated);
  payload.put("referenceProfile",context.snapshot(profile));payload.put("programId",request.programId());
  payload.put("selectionLimit",100);payload.put("evaluatedAt",Instant.now());
  if(profile.getAcademicYearId()==null)payload.put("warning","Annee et profil confirme manquants");
  return payload;
 }
}
