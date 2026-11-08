package com.orientation.orientationapp.ai.comparator.service.impl;
import com.orientation.orientationapp.ai.comparator.model.ComparisonResult;
import com.orientation.orientationapp.ai.comparator.service.ComparisonService;
import com.orientation.orientationapp.ai.comparator.dto.CompareRequest;
import com.orientation.orientationapp.ai.decision.DecisionContext;
import com.orientation.orientationapp.ai.history.DecisionHistoryService;
import com.orientation.orientationapp.modules.university.service.AcademicCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
import static com.orientation.orientationapp.security.service.ResourceAccess.denied;

@Service @RequiredArgsConstructor @Transactional(noRollbackFor=ResponseStatusException.class)
public class DefaultComparisonService implements ComparisonService {
 private final com.orientation.orientationapp.modules.scholarship.service.ScholarshipRecommendationService scholarships;
 private final DecisionContext context;private final DecisionHistoryService history;private final AcademicCatalogService catalog;
 public ComparisonResult compare(CompareRequest request) {
  if(request.programIds()==null||request.programIds().size()<2||request.programIds().size()>6||
      new HashSet<>(request.programIds()).size()!=request.programIds().size())
   throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Select two to six distinct programme UUIDs");
  var owner=context.load(request.candidateId());
  var evaluation=context.evaluate(owner.profile(),request.programIds());var rows=context.results(evaluation);
  for(var row:rows)row.put("scholarships",scholarships.assess(new com.orientation.orientationapp.modules.scholarship.dto.ScholarshipEvaluationRequest(owner.candidate().getId(),(UUID)row.get("programId"),null)).get("scholarships"));
  var differences=differences(rows);
  var warnings=new ArrayList<String>();
  warnings.add("La pertinence pour le candidat ne constitue pas un classement officiel des universites.");
  warnings.add("Les frais restent dans leur devise originale ; aucune conversion ni comparaison de periode n'est supposee.");
  if(owner.profile().getAcademicYearId()==null)warnings.add("Profil non confirme : eligibilite et scores personnels indetermines.");
  var payload=new LinkedHashMap<String,Object>();payload.put("programs",rows);payload.put("differences",differences);
  payload.put("warnings",warnings);payload.put("referenceProfile",context.snapshot(owner.profile()));
  var saved=history.save(owner.candidate(),"COMPARISON",null,payload);
  return new ComparisonResult(saved.id(),"COMPARISON",saved.candidateId(),saved.computedAt(),rows,differences,warnings);
 }
 @Override public ComparisonResult comparePrograms(UUID a,UUID b){return compare(new CompareRequest(null,List.of(a,b)));}
 @Override public ComparisonResult compareUniversities(UUID a,UUID b) {
  var owner=context.load(null);var rows=new ArrayList<Map<String,Object>>();
  for(var id:List.of(a,b)) {
   var university=catalog.university(id);
   if(!Boolean.TRUE.equals(university.getVerified())||!Boolean.TRUE.equals(university.getActive())||
      university.getTenantId()!=null&&!university.getTenantId().equals(owner.candidate().getTenant().getId()))throw denied();
   rows.add(catalog.view(university));
  }
  var payload=new LinkedHashMap<String,Object>();payload.put("universities",rows);
  payload.put("warnings",List.of("Comparaison factuelle : aucune eligibilite ou probabilite universitaire globale."));
  var saved=history.save(owner.candidate(),"COMPARISON",null,payload);
  return new ComparisonResult(saved.id(),"UNIVERSITY_COMPARISON",saved.candidateId(),saved.computedAt(),rows,
      differences(rows),List.of("Aucun classement officiel ou probabilite d'admission n'est deduit."));
 }
 public static Map<String,List<Object>> differences(List<Map<String,Object>> rows) {
  var fields=new LinkedHashSet<String>();rows.forEach(r->fields.addAll(r.keySet()));
  var result=new LinkedHashMap<String,List<Object>>();
  for(var field:fields) {
   if(Set.of("eligibility","scoring","sources","university","faculty","campus").contains(field))continue;
   var values=new ArrayList<Object>();rows.forEach(r->values.add(r.get(field)));
   if(values.stream().distinct().count()>1)result.put(field,values);
  }
  return result;
 }
}
