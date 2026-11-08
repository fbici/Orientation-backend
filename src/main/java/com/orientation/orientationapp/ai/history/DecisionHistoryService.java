package com.orientation.orientationapp.ai.history;
import com.fasterxml.jackson.databind.*;
import com.orientation.orientationapp.security.service.ResourceAccess;
import com.orientation.orientationapp.modules.user.entity.Candidate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional
public class DecisionHistoryService {
 private final ResourceAccess access;
 private final DecisionRecordRepository records;
 private final ObjectMapper json;
 private final com.orientation.orientationapp.modules.orientation.repository.GuideVersionRepository guides;
 private final com.orientation.orientationapp.docintel.document.repository.DocumentVersionRepository documents;
 public record View(UUID id,String kind,UUID candidateId,Long candidateVersion,UUID parentId,Instant computedAt,
                    boolean historical,boolean profileChanged,boolean sourceCurrent,JsonNode result) {}
 public Candidate candidate(UUID id,boolean writing) {
  var c=access.candidate(id==null?access.ownCandidate().getId():id);
  if(!c.getUser().getId().equals(access.current().getId()))access.require(writing?"RECOMMENDATION_EXECUTE":"RECOMMENDATION_READ");
  return c;
 }
 public View save(Candidate c,String kind,UUID parent,Object payload) {
  candidate(c.getId(),true);
  var row=new DecisionRecord();row.setCandidate(c);row.setCandidateVersion(c.getVersion());
  row.setKind(kind);row.setParentId(parent);row.setActorId(access.current().getId());
  try{row.setPayload(json.writeValueAsString(payload));}catch(Exception e){throw new IllegalStateException(e);}
  return view(records.saveAndFlush(row));
 }
 @Transactional(readOnly=true) public View get(UUID id) {return view(authorized(id));}
 public DecisionRecord authorized(UUID id) {
  var row=records.findById(id).orElseThrow(ResourceAccess::denied);
  var c=candidate(row.getCandidate().getId(),false);
  if(row.getKind().startsWith("CHAT")&&!row.getActorId().equals(access.current().getId()))throw ResourceAccess.denied();
  return row;
 }
 @Transactional(readOnly=true) public Page<View> history(UUID id,String kind,int page,int size) {
  var c=candidate(id,false);
  if(kind!=null&&!Set.of("SIMULATION","COMPARISON","SCHOLARSHIP","CHAT_SESSION","CHAT_MESSAGE").contains(kind))
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unknown decision kind");
  var p=PageRequest.of(Math.max(0,page),Math.max(1,Math.min(100,size)));
  // Chat is always personal, including when administrative candidate reads are allowed.
  boolean own=c.getUser().getId().equals(access.current().getId());
  if(!own&&kind==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Select a non-chat decision kind for administrative history");
  return records.findAll((r,q,cb)-> {
   var filters=new ArrayList<jakarta.persistence.criteria.Predicate>();
   filters.add(cb.equal(r.get("candidate").get("id"),c.getId()));
   if(kind!=null)filters.add(cb.equal(r.get("kind"),kind));
   filters.add(cb.or(cb.notLike(r.get("kind"),"CHAT%"),cb.equal(r.get("actorId"),access.current().getId())));
   return cb.and(filters.toArray(jakarta.persistence.criteria.Predicate[]::new));
  },PageRequest.of(Math.max(0,page),Math.max(1,Math.min(100,size)),Sort.by("createdAt","id").descending())).map(this::view);
 }
 @Transactional(readOnly=true) public Page<View> children(UUID parent,int page,int size) {
  authorized(parent);
  return records.findByParentIdOrderByCreatedAtAscIdAsc(parent,PageRequest.of(Math.max(0,page),Math.max(1,Math.min(100,size)))).map(this::view);
 }
 private View view(DecisionRecord row) {
  try{return new View(row.getId(),row.getKind(),row.getCandidate().getId(),row.getCandidateVersion(),row.getParentId(),
   row.getCreatedAt(),true,!Objects.equals(row.getCandidateVersion(),row.getCandidate().getVersion()),sourceCurrent(json.readTree(row.getPayload())),json.readTree(row.getPayload()));}
  catch(java.io.IOException e){throw new IllegalStateException(e);}
 }

 private boolean sourceCurrent(JsonNode payload) {
  var ids=new LinkedHashSet<UUID>();guideIds(payload,ids);
  if(ids.isEmpty())return false;
  var today=java.time.LocalDate.now();
  for(var id:ids) {
   var v=guides.findById(id).orElse(null);
   if(v==null||v.getStatus()!=com.orientation.orientationapp.common.enums.GuideVersionStatus.ACTIVE||!Boolean.TRUE.equals(v.getActive())||
      v.getEffectiveDate()==null||v.getEffectiveDate().isAfter(today)||v.getExpiryDate()!=null&&v.getExpiryDate().isBefore(today)||
      v.getOrientationGuide()==null||!Boolean.TRUE.equals(v.getOrientationGuide().getActive())||v.getDocumentVersionId()==null)return false;
   var document=documents.findById(v.getDocumentVersionId()).orElse(null);
   if(document==null||!Boolean.TRUE.equals(document.getActive()))return false;
  }
  return true;
 }
 private void guideIds(JsonNode node,Set<UUID> ids) {
  if(node==null)return;
  if(node.isObject())node.fields().forEachRemaining(e->{
   if(e.getKey().equals("guideVersionId")&&e.getValue().isTextual()) {
    try{ids.add(UUID.fromString(e.getValue().asText()));}catch(IllegalArgumentException ignored){/* Not an identifier. */}
   }else guideIds(e.getValue(),ids);
  });else if(node.isArray())node.forEach(v->guideIds(v,ids));
 }
}
