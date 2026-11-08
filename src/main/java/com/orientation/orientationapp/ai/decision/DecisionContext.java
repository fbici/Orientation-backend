package com.orientation.orientationapp.ai.decision;
import com.orientation.orientationapp.ai.history.DecisionHistoryService;
import com.orientation.orientationapp.recengine.profile.analyzer.impl.DefaultProfileAnalyzer;
import com.orientation.orientationapp.recengine.profile.model.AcademicProfile;
import com.orientation.orientationapp.modules.user.entity.Candidate;
import com.orientation.orientationapp.modules.recommendation.service.PersistedRecommendationService;
import com.orientation.orientationapp.modules.university.service.AcademicCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class DecisionContext {
 private final DecisionHistoryService history;
 private final DefaultProfileAnalyzer analyzer;
 private final PersistedRecommendationService recommendations;
 private final AcademicCatalogService catalog;
 private final com.orientation.orientationapp.modules.university.service.AcademicProvenanceService provenance;
 public record Context(Candidate candidate,AcademicProfile profile) {}
 public Context load(UUID id) {var c=history.candidate(id,true);return new Context(c,analyzer.analyze(c.getId()));}
 public PersistedRecommendationService.Evaluation evaluate(AcademicProfile profile,List<UUID> selected) {
  return recommendations.evaluate(profile,selected);
 }
 public Map<String,Object> snapshot(AcademicProfile p) {
  var data=new LinkedHashMap<String,Object>();
  data.put("candidateId",p.getCandidateId());data.put("countryId",p.getCountry());data.put("profileSource","SELF_CONFIRMED");data.put("academicYearId",p.getAcademicYearId());
  data.put("gradesAcademicYearId",p.getGradesAcademicYearId());data.put("generalAverage",p.getGeneralAverage());
  data.put("subjectGrades",p.getSubjectGrades());data.put("bacType",p.getBacType());data.put("bacSeries",p.getBacSeries());
  data.put("diploma",p.getDiploma());data.put("educationLevel",p.getEducationLevel());
  data.put("preferredFields",p.getPreferredFields());data.put("preferredCountries",p.getPreferredCountries());
  data.put("preferredLanguages",p.getPreferredLanguages());data.put("language",p.getLanguage());data.put("budget",p.getBudget());
  data.put("nationality",p.getNationality());data.put("householdIncome",p.getHouseholdIncome());
  data.put("incomeCurrency",p.getIncomeCurrency());data.put("incomePeriod",p.getIncomePeriod());
  data.put("targetProgramId",p.getTargetProgramId());data.put("targetUniversityId",p.getTargetUniversityId());data.put("targetStudyField",p.getTargetStudyField());
  data.put("eligibilityVersion","F3.2-1");data.put("scoringVersion","F3.1-1");
  data.put("gradeScaleId",p.getGradeScale()==null?null:p.getGradeScale().getId());
  if(p.getGradeScale()!=null) {
   var scale=new LinkedHashMap<String,Object>();
   scale.put("min",p.getGradeScale().getMinScore());scale.put("max",p.getGradeScale().getMaxScore());
   scale.put("normalizeTo",p.getGradeScale().getNormalizeTo());data.put("sourceScale",scale);
  }
  return data;
 }
 public List<Map<String,Object>> results(PersistedRecommendationService.Evaluation evaluation) {
  var result=new ArrayList<Map<String,Object>>();
  for(var ranked:evaluation.ranked()) {
   var p=evaluation.programmes().stream().filter(x->x.getId().equals(ranked.getProgramId())).findFirst().orElseThrow();
   var item=new LinkedHashMap<String,Object>(catalog.view(p));
   item.put("programId",p.getId());item.put("compatibilityRank",ranked.getRank());
   item.put("score",ranked.getScore().getFinalScore());item.put("dataCoverage",ranked.getScore().getConfidence());
   item.put("scoring",ranked.getScore());item.put("eligibility",evaluation.eligibility().get(p.getId()));
   item.put("eligibilityStatus",evaluation.eligibility().get(p.getId()).getStatus());
   var guide=evaluation.versions().get(p.getId());item.put("guideVersionId",guide==null?null:guide.getId());
   item.put("admissionProbability",null);
   item.put("sources",provenance.sources("programs",p.getId(),guide==null?null:guide.getId(),true));
   item.put("university",catalog.view(p.getFaculty().getCampus().getUniversity()));
   item.put("campus",catalog.view(p.getFaculty().getCampus()));
   item.put("faculty",catalog.view(p.getFaculty()));
   result.add(item);
  }
  return result;
 }
}
