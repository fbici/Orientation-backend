package com.orientation.orientationapp.intelligence.knowledge;
import com.orientation.orientationapp.intelligence.extraction.EntityExtractionService.ExtractedEntities;
import com.orientation.orientationapp.knowledge.entity.*;
import com.orientation.orientationapp.knowledge.repository.*;
import com.orientation.orientationapp.modules.orientation.entity.GuideVersion;
import com.orientation.orientationapp.modules.orientation.entity.AdmissionCriterion;
import com.orientation.orientationapp.modules.orientation.repository.AdmissionCriterionRepository;
import com.orientation.orientationapp.modules.university.repository.ProgramRepository;
import com.orientation.orientationapp.modules.scholarship.repository.ScholarshipRepository;
import com.orientation.orientationapp.docintel.document.entity.ExtractedFact;
import com.orientation.orientationapp.docintel.document.service.FactPayloadMapper;
import com.orientation.orientationapp.modules.university.service.CatalogAccess;
import com.orientation.orientationapp.intelligence.indexing.SearchIndexService;
import com.orientation.orientationapp.common.enums.GuideVersionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.PageRequest;
import java.util.*;
import static com.orientation.orientationapp.modules.university.service.AcademicCatalogService.row;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class KnowledgeGraphService {
 private final KnowledgeNodeRepository nodes;
 private final KnowledgeRelationRepository relations;
 private final ProgramRepository programs;
 private final ScholarshipRepository scholarships;
 private final AdmissionCriterionRepository criteria;
 private final CatalogAccess access;
 private final FactPayloadMapper payloads;
 private final SearchIndexService indices;
 public Specification<KnowledgeNode> visible() {
  UUID tenant=access.tenant();boolean global=access.global();
  return (root,query,cb) -> {
   var active=query.subquery(UUID.class);var version=active.from(GuideVersion.class);
   active.select(version.get("id")).where(cb.equal(version.get("status"),GuideVersionStatus.ACTIVE));
   return cb.and(cb.isTrue(root.get("verified")),cb.isTrue(root.get("active")),root.get("guideVersionId").in(active),
    global?cb.conjunction():tenant==null?cb.isNull(root.get("tenantId")):cb.or(cb.isNull(root.get("tenantId")),cb.equal(root.get("tenantId"),tenant)));
  };
 }
 public Map<String,Object> view(KnowledgeNode n) {
  var holder=new ExtractedFact();holder.setPayload(n.getProperties());
  return row("id",n.getId(),"entityId",n.getEntityId(),"name",n.getName(),"label",n.getName(),"type",n.getEntityType(),
   "nodeType",n.getNodeType(),"description",n.getDescription(),"guideVersionId",n.getGuideVersionId(),
   "countryId",n.getCountryId(),"academicYearId",n.getAcademicYearId(),"sourceDocumentId",n.getSourceDocumentId(),
   "verificationStatus","VALIDATED","metadata",payloads.values(holder));
 }
 public List<KnowledgeNode> search(String query) {
  if(query==null || query.length()>200)throw com.orientation.orientationapp.modules.university.service.AcademicCatalogService.invalid("Search must contain at most 200 characters");
  return nodes.findAll(visible().and((root,q,cb)->cb.like(cb.lower(root.get("name")),"%"+query.toLowerCase(Locale.ROOT).replace("%","\\%").replace("_","\\_")+"%",'\\')),
   PageRequest.of(0,100,org.springframework.data.domain.Sort.by("name"))).getContent();
 }
 public List<KnowledgeNode> getRelatedNodes(UUID nodeId) {
  if(nodes.count(visible().and((r,q,cb)->cb.equal(r.get("id"),nodeId)))==0)throw com.orientation.orientationapp.security.service.ResourceAccess.denied();
  var links=relations.findBySourceId(nodeId);var ids=links.stream().filter(r -> Boolean.TRUE.equals(r.getActive())).map(r->r.getTarget().getId()).toList();
  if(ids.isEmpty())return List.of();
  return nodes.findAll(visible().and((r,q,cb)->r.get("id").in(ids)),PageRequest.of(0,100)).getContent();
 }
 public Map<String,Object> graph() {
  var selected=nodes.findAll(visible(),PageRequest.of(0,500,org.springframework.data.domain.Sort.by("name")));
  var ids=selected.stream().map(KnowledgeNode::getId).toList();
  var edges=ids.isEmpty()?List.<Map<String,Object>>of():relations.findAll((r,q,cb)->cb.and(cb.isTrue(r.get("active")),r.get("source").get("id").in(ids),r.get("target").get("id").in(ids)),PageRequest.of(0,2000))
   .stream().map(r->row("id",r.getId(),"source",r.getSource().getId(),"target",r.getTarget().getId(),"label",r.getRelationType())).toList();
  return row("nodes",selected.stream().map(this::view).toList(),"edges",edges,"totalNodes",selected.getTotalElements(),"truncated",selected.hasNext(),"searchMode","TEXT","embeddingsAvailable",false);
 }
 @Transactional public void publish(ExtractedFact fact,GuideVersion version,String type,UUID entityId,String name,Map<String,Object> attributes) {
  if(!"VALIDATED".equals(fact.getStatus()) || fact.getReviewedBy()==null)throw new IllegalArgumentException("Unvalidated source fact");
  var primary=node(fact,version,type,entityId,name,attributes);
  var guide=node(fact,version,"GUIDE_VERSION",version.getId(),version.getOrientationGuide().getTitle(),row("version",version.getVersionNumber()));
  link(primary,guide,"DOCUMENTED_BY",fact);
  if(type.equals("PROGRAM")) {
   var p=programs.findById(entityId).orElseThrow();var f=p.getFaculty();var u=f.getCampus().getUniversity();
   var university=node(fact,version,"UNIVERSITY",u.getId(),u.getName(),row("id",u.getId(),"name",u.getName()));
   var faculty=node(fact,version,"FACULTY",f.getId(),f.getName(),row("id",f.getId(),"name",f.getName()));
   link(university,primary,"OFFERS",fact);link(primary,faculty,"BELONGS_TO",fact);
  }
  if(type.equals("ADMISSION_CRITERION")) {
   AdmissionCriterion c=criteria.findById(entityId).orElseThrow();var p=c.getProgram();
   if(p!=null)link(node(fact,version,"PROGRAM",p.getId(),p.getName(),row("id",p.getId(),"name",p.getName())),primary,"REQUIRES",fact);
  }
  if(type.equals("SCHOLARSHIP")) {
   var s=scholarships.findById(entityId).orElseThrow();
   if(s.getProgramId()!=null) {
    var p=programs.findById(s.getProgramId()).orElseThrow();
    link(primary,node(fact,version,"PROGRAM",p.getId(),p.getName(),row("id",p.getId(),"name",p.getName())),"FUNDS",fact);
   }
  }
 }
 @SuppressWarnings("unchecked")
 private KnowledgeNode node(ExtractedFact f,GuideVersion v,String type,UUID id,String name,Map<String,Object> attributes) {
  var n=nodes.findByEntityTypeAndEntityIdAndGuideVersionId(type,id,v.getId()).orElseGet(KnowledgeNode::new);
  List<Map<String,Object>> sources=new ArrayList<>();
  var mergedAttributes=new LinkedHashMap<String,Object>();
  if(n.getProperties()!=null) {
   var old=new ExtractedFact();old.setPayload(n.getProperties());var previous=payloads.values(old);Object list=previous.get("sources");
   if(previous.get("entity") instanceof Map<?,?> oldEntity)oldEntity.forEach((key,value)->mergedAttributes.put(String.valueOf(key),value));
   if(list instanceof List<?> values)for(Object item:values)if(item instanceof Map<?,?> map)sources.add((Map<String,Object>)map);
  }
  if(sources.stream().noneMatch(s->f.getId().toString().equals(String.valueOf(s.get("factId")))))
   sources.add(row("factId",f.getId(),"kind",f.getKind(),"documentId",f.getDocument().getId(),"documentVersionId",f.getDocumentVersion().getId(),
    "checksum",f.getDocumentVersion().getChecksum(),"sourceVersion",f.getDocumentVersion().getVersionNumber(),"pageNumber",f.getPageNumber(),
    "passage",f.getSourcePassage(),"values",payloads.values(f),"guideVersionId",v.getId(),"guideVersionNumber",v.getVersionNumber(),
    "academicYearId",v.getOrientationGuide().getAcademicYear().getId(),"reviewStatus","VALIDATED","reviewedAt",f.getReviewedAt()));
  attributes.forEach((key,value)->{if(value!=null)mergedAttributes.put(key,value);});
  UUID programId=type.equals("PROGRAM")?id:mergedAttributes.get("programId")==null?null:payloads.uuid(mergedAttributes.get("programId"));
  UUID universityId=type.equals("UNIVERSITY")?id:mergedAttributes.get("universityId")==null?null:payloads.uuid(mergedAttributes.get("universityId"));
  if(type.equals("ACADEMIC_FACT") || type.equals("GUIDE_VERSION")){programId=null;universityId=null;}
  if(programId!=null && universityId==null)universityId=programs.findById(programId).orElseThrow().getFaculty().getCampus().getUniversity().getId();
  n.setProgramId(programId);n.setUniversityId(universityId);
  n.setEntityId(id);n.setEntityType(type);n.setNodeType(type.equals("ACADEMIC_FACT")?f.getKind():type);n.setName(name.length()>200?name.substring(0,200):name);
  n.setDescription(mergedAttributes.get("description") instanceof String description?description:name);
  n.setProperties(payloads.encode(row("entity",mergedAttributes,"sources",sources,"searchMode","TEXT")));
  n.setTenantId(UUID.fromString(f.getDocument().getTenantId()));n.setGuideVersionId(v.getId());
  n.setCountryId(v.getOrientationGuide().getCountry().getId());n.setAcademicYearId(v.getOrientationGuide().getAcademicYear().getId());
  n.setSourceDocumentId(f.getDocument().getId());n.setVerified(true);n.setActive(true);n.setEmbedding(null);
  nodes.saveAndFlush(n);indices.index(n);return n;
 }
 private void link(KnowledgeNode source,KnowledgeNode target,String type,ExtractedFact fact) {
  if(source.getId().equals(target.getId()))return;
  var relation=relations.findBySourceIdAndTargetIdAndRelationType(source.getId(),target.getId(),type).orElseGet(KnowledgeRelation::new);
  relation.setSource(source);relation.setTarget(target);relation.setRelationType(type);relation.setWeight(null);
  relation.setProperties(payloads.encode(row("factId",fact.getId(),"documentId",fact.getDocument().getId(),"pageNumber",fact.getPageNumber())));
  relation.setActive(true);relations.saveAndFlush(relation);
 }
 @Deprecated public void updateGraph(ExtractedEntities ignored,UUID documentId) {
  throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT,"Knowledge publication requires validated source facts");
 }
}
