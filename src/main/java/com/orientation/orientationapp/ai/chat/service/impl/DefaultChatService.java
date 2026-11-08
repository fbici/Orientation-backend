package com.orientation.orientationapp.ai.chat.service.impl;
import com.orientation.orientationapp.ai.chat.model.*;
import com.orientation.orientationapp.ai.chat.service.*;
import com.orientation.orientationapp.ai.history.*;
import com.orientation.orientationapp.ai.decision.DecisionContext;
import com.orientation.orientationapp.ai.comparator.dto.CompareRequest;
import com.orientation.orientationapp.ai.comparator.service.ComparisonService;
import com.orientation.orientationapp.modules.scholarship.service.ScholarshipRecommendationService;
import com.orientation.orientationapp.modules.scholarship.dto.ScholarshipEvaluationRequest;
import com.orientation.orientationapp.modules.recommendation.service.PersistedRecommendationService;
import com.orientation.orientationapp.recengine.simulation.engine.impl.DefaultSimulationEngine;
import com.orientation.orientationapp.knowledge.search.service.SemanticSearchService;
import com.orientation.orientationapp.security.service.ResourceAccess;
import com.fasterxml.jackson.databind.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional
public class DefaultChatService implements ChatService {
 private final DecisionHistoryService history;
 private final DecisionContext context;
 private final ResourceAccess access;
 private final AssistantIntentRouter router;
 private final PersistedRecommendationService recommendations;
 private final ComparisonService comparisons;
 private final ScholarshipRecommendationService scholarships;
 private final DefaultSimulationEngine simulations;
 private final SemanticSearchService search;
 private final ObjectMapper json;
 public ChatSession createSession(String userId) {access.personalUser(userId);return createSessionFor(null);}
 public ChatSession createSessionFor(UUID candidateId) {
  var c=history.candidate(candidateId,true);
  var row=history.save(c,"CHAT_SESSION",null,Map.of("assistantVersion","F3.2-1","mode","DETERMINISTIC"));
  return session(row);
 }
 public ChatMessage sendMessage(UUID sessionId,String userId,String message) {
  access.personalUser(userId);return ask(sessionId,new AskRequest(message,null,null,null));
 }
 private DecisionHistoryService.View root(UUID id) {
  var row=history.get(id);
  if(!"CHAT_SESSION".equals(row.kind()))throw ResourceAccess.denied();
  return row;
 }
 public ChatSession getSession(UUID id) {
  var s=session(root(id));var page=history.children(id,0,20);
  for(var row:page) s.addMessage(message(row));
  return s;
 }
 private ChatSession session(DecisionHistoryService.View row) {
  return ChatSession.builder().sessionId(row.id()).userId(access.current().getId()).candidateId(row.candidateId())
   .createdAt(row.computedAt()).lastActivityAt(row.computedAt()).messages(new ArrayList<>()).build();
 }
 public ChatMessage ask(UUID id,AskRequest request) {
  var session=root(id);
  if(request.message()==null||request.message().isBlank()||request.message().length()>2000)
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"A question of 1 to 2000 characters is required");
  var ctx=context.load(session.candidateId());var intent=router.detect(request.message());
  Object data=Map.of();UUID decisionId=null;String status="ANSWERED";
  String answer="Resultat calcule a partir du profil et des sources disponibles. Aucune admission ou bourse garantie.";
  try {
   switch(intent) {
    case RECOMMENDATIONS -> {
     if(ctx.profile().getAcademicYearId()==null)throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Confirmer le profil et son annee d'admission.");
     data=context.results(context.evaluate(ctx.profile(),null));
    }
    case EXPLANATION,SUBJECTS -> {
     var item=request.recommendationId()==null?
      recommendations.list(session.candidateId(),null,0,1).getContent().stream().findFirst().orElse(null):
      recommendations.get(request.recommendationId());
     if(item==null)throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Generer ou identifier une recommandation.");
     if(!session.candidateId().equals(item.candidateId()))throw ResourceAccess.denied();
     data=item;decisionId=item.id();
     answer="Explication historique des criteres et notes de la recommandation selectionnee; aucun seuil supplementaire n'est invente.";
     if(item.stale()||!item.sourceCurrent()) {status="HISTORICAL";answer+=" Recalcul requis: profil ou source modifie.";}
    }
    case COMPARISON -> {
     if(request.programIds()==null||request.programIds().size()<2) {
      status="NEEDS_INPUT";answer="Fournir entre deux et six identifiants de programmes dans programIds.";
     } else {var result=comparisons.compare(new CompareRequest(session.candidateId(),request.programIds()));data=result;decisionId=result.id();}
    }
    case SIMULATION -> {
     if(request.simulation()==null) {status="NEEDS_INPUT";answer="Fournir les modifications explicites dans simulation, avec le bareme du profil confirme.";}
     else {
      var sim=request.simulation();
      if(sim.getCandidateId()!=null&&!session.candidateId().equals(sim.getCandidateId()))throw ResourceAccess.denied();
      sim.setCandidateId(session.candidateId());var result=simulations.simulate(sim);data=result;decisionId=result.id();
      answer="Simulation hypothetique: le profil confirme et les recommandations reelles restent inchanges.";
     }
    }
    case SCHOLARSHIPS -> {
     UUID program=request.programIds()==null||request.programIds().isEmpty()?null:request.programIds().getFirst();
     var result=scholarships.evaluate(new ScholarshipEvaluationRequest(session.candidateId(),program,null));
     data=result;decisionId=result.id();answer="Ouverture et eligibilite sont evaluees separement. Une condition inconnue ne vaut pas eligibilite.";
    }
    case SEARCH -> {
     data=search.searchFiltered(request.message().substring(0,Math.min(200,request.message().length())),null,10,0,null,null,null,ctx.profile().getAcademicYearId(),null);
     answer="Faits documentaires disponibles dans les sources publiees. Ces faits ne constituent pas une decision d'admission.";
    }
    case UNSUPPORTED -> {status="UNSUPPORTED";answer="Demande non prise en charge. Utiliser recommandations, explication, matieres, bourses, comparaison, simulation ou recherche documentaire.";}
   }
  } catch(ResponseStatusException e) {
   if(e.getStatusCode().value()!=422)throw e;
   status="INSUFFICIENT_DATA";answer=e.getReason()==null?"Donnees insuffisantes.":e.getReason();data=Map.of();
  }
  var tree=json.valueToTree(data);var sources=new ArrayList<Object>();collectSources(tree,sources);
  var payload=new LinkedHashMap<String,Object>();payload.put("question",request.message());payload.put("intent",intent.name());
  payload.put("status",status);payload.put("content",answer);payload.put("data",tree);payload.put("sources",sources);
  payload.put("decisionId",decisionId);payload.put("assistantVersion","F3.2-1");
  return message(history.save(ctx.candidate(),"CHAT_MESSAGE",id,payload));
 }
 private ChatMessage message(DecisionHistoryService.View row) {
  var n=row.result();
  return ChatMessage.builder().id(row.id()).role("ASSISTANT").sessionId(row.parentId().toString())
   .timestamp(row.computedAt()).question(n.path("question").asText()).content(n.path("content").asText())
   .intent(n.path("intent").asText()).status(n.path("status").asText())
   .decisionId(n.path("decisionId").isTextual()?UUID.fromString(n.path("decisionId").asText()):null)
   .data(n.path("data")).sources(json.convertValue(n.path("sources"),new com.fasterxml.jackson.core.type.TypeReference<List<Object>>(){})).build();
 }
 private void collectSources(JsonNode n,List<Object> out) {
  if(n==null)return;
  if(n.isObject()) {
   if(n.hasNonNull("sourceFactId"))out.add(json.convertValue(n,Map.class));
   n.fields().forEachRemaining(e->{if(e.getKey().equals("sources")&&e.getValue().isArray())e.getValue().forEach(v->{var value=json.convertValue(v,Object.class);if(!out.contains(value))out.add(value);});
    else collectSources(e.getValue(),out);});
  } else if(n.isArray())n.forEach(v->collectSources(v,out));
 }
}
