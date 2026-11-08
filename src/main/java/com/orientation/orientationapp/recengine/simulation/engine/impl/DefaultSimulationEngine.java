package com.orientation.orientationapp.recengine.simulation.engine.impl;
import com.orientation.orientationapp.ai.decision.DecisionContext;
import com.orientation.orientationapp.ai.history.DecisionHistoryService;
import com.orientation.orientationapp.recengine.recommendation.dto.request.SimulationRequest;
import com.orientation.orientationapp.recengine.profile.model.AcademicProfile;
import com.orientation.orientationapp.recengine.profile.normalization.GradeNormalizer;
import com.orientation.orientationapp.modules.orientation.repository.*;
import com.orientation.orientationapp.modules.university.repository.CountryRepository;
import com.orientation.orientationapp.modules.orientation.entity.GradeScale;
import com.orientation.orientationapp.common.enums.BacType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
import java.math.BigDecimal;
import java.time.Instant;

@Service @RequiredArgsConstructor @Transactional(noRollbackFor=ResponseStatusException.class)
public class DefaultSimulationEngine {
 private final DecisionContext context;
 private final DecisionHistoryService history;
 private final GradeNormalizer normalizer;
 private final AcademicYearRepository years;
 private final SubjectRepository subjects;
 private final CountryRepository countries;
 public DecisionHistoryService.View simulate(SimulationRequest request) {
  var original=context.load(request.getCandidateId());
  if(original.profile().getAcademicYearId()==null)throw invalid("A confirmed reference profile is required");
  var copy=copy(original.profile());apply(copy,request);
  var before=context.evaluate(original.profile(),request.getProgramIds());
  var after=context.evaluate(copy,request.getProgramIds());
  if(after.versions().isEmpty())throw invalid("No applicable published guide for this scenario year");
  var beforeRows=context.results(before);var afterRows=context.results(after);
  var data=new LinkedHashMap<String,Object>();
  data.put("type","SIMULATION");data.put("isSimulation",true);data.put("computedAt",Instant.now());
  data.put("referenceProfile",context.snapshot(original.profile()));data.put("simulatedProfile",context.snapshot(copy));
  data.put("modifications",request);data.put("before",beforeRows);data.put("after",afterRows);
  data.put("differences",differences(beforeRows,afterRows));
  data.put("disclaimer","Scenario hypothetique : aucune modification du profil confirme, aucune admission garantie.");
  return history.save(original.candidate(),"SIMULATION",null,data);
 }
 public AcademicProfile copy(AcademicProfile p) {
  return p.toBuilder().subjectGrades(p.getSubjectGrades()==null?null:new LinkedHashMap<>(p.getSubjectGrades()))
   .subjectGradeScales(p.getSubjectGradeScales()==null?null:new LinkedHashMap<>(p.getSubjectGradeScales()))
   .preferredFields(p.getPreferredFields()==null?null:new ArrayList<>(p.getPreferredFields()))
   .preferredCountries(p.getPreferredCountries()==null?null:new ArrayList<>(p.getPreferredCountries()))
   .preferredLanguages(p.getPreferredLanguages()==null?null:new ArrayList<>(p.getPreferredLanguages())).build();
 }
 public void apply(AcademicProfile p,SimulationRequest r) {
  if(r.getProgramIds()!=null&&(r.getProgramIds().isEmpty()||new HashSet<>(r.getProgramIds()).size()!=r.getProgramIds().size()))throw invalid("Unique programme IDs are required");
  if(r.getAcademicYearId()!=null) {
   if(!years.existsById(r.getAcademicYearId()))throw invalid("Unknown admission year");
   p.setAcademicYearId(r.getAcademicYearId());
  }
  if(r.getModifiedGeneralAverage()!=null&&r.getModifiedBacAverage()!=null&&
     r.getModifiedGeneralAverage().compareTo(r.getModifiedBacAverage())!=0)throw invalid("Conflicting averages");
  var average=r.getModifiedGeneralAverage()!=null?r.getModifiedGeneralAverage():r.getModifiedBacAverage();
  if(average!=null) {grade(average,p.getGradeScale());p.setGeneralAverage(average);}
  if(r.getModifiedGrades()!=null) {
   var available=p.getGradeScale()==null?List.<com.orientation.orientationapp.modules.orientation.entity.Subject>of():
    subjects.findByGradeScaleId(p.getGradeScale().getId());
   var marks=p.getSubjectGrades()==null?new LinkedHashMap<String,BigDecimal>():new LinkedHashMap<>(p.getSubjectGrades());
   for(var entry:r.getModifiedGrades().entrySet()) {
    if(available.stream().noneMatch(s->Boolean.TRUE.equals(s.getActive())&&entry.getKey().equals(s.getCode())))throw invalid("Unknown subject code in the configured scale");
    var source=p.getSubjectGradeScales()==null?p.getGradeScale():p.getSubjectGradeScales().get(entry.getKey());
    grade(entry.getValue(),source);marks.put(entry.getKey(),entry.getValue());
   }
   p.setSubjectGrades(marks);
  }
  if(r.getModifiedBacType()!=null) {
   try{BacType.valueOf(r.getModifiedBacType());}catch(IllegalArgumentException e){throw invalid("Unknown bac category");}
   p.setBacType(r.getModifiedBacType());
  }
  if(r.getModifiedBacSeries()!=null)p.setBacSeries(r.getModifiedBacSeries());
  if(r.getModifiedFields()!=null)p.setPreferredFields(new ArrayList<>(r.getModifiedFields()));
  if(r.getModifiedCountries()!=null) {
   r.getModifiedCountries().forEach(this::country);
   p.setPreferredCountries(new ArrayList<>(r.getModifiedCountries()));
  }
  if(r.getModifiedCountry()!=null) {country(r.getModifiedCountry());p.setPreferredCountries(List.of(r.getModifiedCountry()));}
  if(r.getModifiedLanguages()!=null)p.setPreferredLanguages(new ArrayList<>(r.getModifiedLanguages()));
  if(r.getModifiedLanguage()!=null){p.setLanguage(r.getModifiedLanguage());p.setPreferredLanguages(List.of(r.getModifiedLanguage()));}
  if(r.getModifiedBudget()!=null) {if(r.getModifiedBudget().signum()<0)throw invalid("Negative budget");p.setBudget(r.getModifiedBudget());}
 }
 private void country(String id) {
  try{if(!countries.existsById(UUID.fromString(id)))throw invalid("Unknown destination country");}
  catch(IllegalArgumentException e){throw invalid("Destination must be a country UUID");}
 }
 private void grade(BigDecimal value,GradeScale scale) {
  if(normalizer.convert(value,scale,scale).isEmpty())throw invalid("An explicit scale and an in-range simulated grade are required");
 }
 public static List<Map<String,Object>> differences(List<Map<String,Object>> before,List<Map<String,Object>> after) {
  var previous=new LinkedHashMap<Object,Map<String,Object>>();var next=new LinkedHashMap<Object,Map<String,Object>>();
  before.forEach(x->previous.put(x.get("programId"),x));after.forEach(x->next.put(x.get("programId"),x));
  var ids=new LinkedHashSet<>(previous.keySet());ids.addAll(next.keySet());var changes=new ArrayList<Map<String,Object>>();
  for(var id:ids) {
   var a=previous.get(id);var b=next.get(id);var change=new LinkedHashMap<String,Object>();
   Object oldStatus=a==null?null:a.get("eligibilityStatus"),newStatus=b==null?null:b.get("eligibilityStatus");
   change.put("programId",id);change.put("beforeEligibility",oldStatus);change.put("afterEligibility",newStatus);
   change.put("eligibilityChanged",!Objects.equals(oldStatus,newStatus));
   var oldScore=a==null?null:(BigDecimal)a.get("score");var newScore=b==null?null:(BigDecimal)b.get("score");
   change.put("scoreDelta",oldScore==null||newScore==null?null:newScore.subtract(oldScore));
   change.put("becameEligible","ELIGIBLE".equals(Objects.toString(newStatus,null))&&!"ELIGIBLE".equals(Objects.toString(oldStatus,null)));
   change.put("becameNotEligible","NOT_ELIGIBLE".equals(Objects.toString(newStatus,null))&&"ELIGIBLE".equals(Objects.toString(oldStatus,null)));
   change.put("stillInsufficient","INSUFFICIENT_DATA".equals(Objects.toString(oldStatus,null))&&"INSUFFICIENT_DATA".equals(Objects.toString(newStatus,null)));
   change.put("explanation",b==null?"Programme absent du catalogue applicable au scenario":b.get("eligibility"));
   changes.add(change);
  }
  return changes;
 }
 private ResponseStatusException invalid(String message){return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,message);}
}
