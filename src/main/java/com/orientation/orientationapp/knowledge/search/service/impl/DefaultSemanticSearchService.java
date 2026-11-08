package com.orientation.orientationapp.knowledge.search.service.impl;
import com.orientation.orientationapp.knowledge.search.service.SemanticSearchService;
import com.orientation.orientationapp.knowledge.entity.SearchIndex;
import com.orientation.orientationapp.knowledge.repository.SearchIndexRepository;
import com.orientation.orientationapp.modules.orientation.entity.GuideVersion;
import com.orientation.orientationapp.common.enums.GuideVersionStatus;
import com.orientation.orientationapp.modules.university.service.CatalogAccess;
import com.orientation.orientationapp.docintel.document.service.FactPayloadMapper;
import com.orientation.orientationapp.docintel.document.entity.ExtractedFact;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import lombok.RequiredArgsConstructor;
import java.util.*;
import static com.orientation.orientationapp.modules.university.service.AcademicCatalogService.row;
import static com.orientation.orientationapp.modules.university.service.AcademicCatalogService.invalid;
/** Existing search entry point: explicit SQL text search, never fabricated embeddings or semantic confidence. */
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class DefaultSemanticSearchService implements SemanticSearchService {
 private final SearchIndexRepository indices;
 private final CatalogAccess access;
 private final FactPayloadMapper payloads;
 @Override public List<Map<String,Object>> search(String query,int limit){return search(query,null,limit);}
 @Override public List<Map<String,Object>> search(String query,String type,int limit) {
  return searchFiltered(query,type,limit,0,null,null,null,null,null);
 }
 @Override public List<Map<String,Object>> searchFiltered(String query,String type,int limit,int page,
  UUID countryId,UUID programId,UUID universityId,UUID academicYearId,UUID sourceId) {
  if(query==null || query.length()>200 || limit<1 || limit>100 || page<0 || page>10000)throw invalid("Invalid text search or pagination");
  UUID owner=access.tenant();boolean global=access.global();
  Specification<SearchIndex> filter=(root,q,cb)-> {
   var tests=new ArrayList<jakarta.persistence.criteria.Predicate>();
   tests.add(cb.isTrue(root.get("verified")));tests.add(cb.isTrue(root.get("active")));
   var versions=q.subquery(UUID.class);var version=versions.from(GuideVersion.class);
   versions.select(version.get("id")).where(sourceId==null?cb.equal(version.get("status"),GuideVersionStatus.ACTIVE):
    version.get("status").in(GuideVersionStatus.ACTIVE,GuideVersionStatus.ARCHIVED));
   tests.add(root.get("guideVersionId").in(versions));
   var tenant=root.get("tenantId");
   if(!global)tests.add(owner==null?cb.isNull(tenant):cb.or(cb.isNull(tenant),cb.equal(tenant,owner)));
   String pattern="%"+query.strip().toLowerCase(Locale.ROOT).replace("%","\\%").replace("_","\\_")+"%";
   tests.add(cb.or(cb.like(cb.lower(root.get("name")),pattern,'\\'),cb.like(cb.lower(root.get("content")),pattern,'\\')));
   if(type!=null && !type.isBlank())tests.add(cb.or(cb.equal(root.get("entityType"),type.toUpperCase(Locale.ROOT)),cb.equal(root.get("nodeType"),type.toUpperCase(Locale.ROOT))));
   if(countryId!=null)tests.add(cb.equal(root.get("countryId"),countryId));
   if(programId!=null)tests.add(cb.equal(root.get("programId"),programId));
   if(universityId!=null)tests.add(cb.equal(root.get("universityId"),universityId));
   if(academicYearId!=null)tests.add(cb.equal(root.get("academicYearId"),academicYearId));
   if(sourceId!=null)tests.add(cb.equal(root.get("sourceDocumentId"),sourceId));
   return cb.and(tests.toArray(jakarta.persistence.criteria.Predicate[]::new));
  };
  return indices.findAll(filter,PageRequest.of(page,limit,Sort.by("name"))).stream().map(this::view).toList();
 }
 private Map<String,Object> view(SearchIndex i) {
  var source=new ExtractedFact();source.setPayload(i.getMetadata());
  return row("id",i.getNodeId(),"nodeId",i.getNodeId(),"indexId",i.getId(),"entityId",i.getEntityId(),
   "type",i.getNodeType(),"entityType",i.getEntityType(),"name",i.getName(),"content",i.getContent(),
   "countryId",i.getCountryId(),"programId",i.getProgramId(),"universityId",i.getUniversityId(),
   "academicYearId",i.getAcademicYearId(),"sourceDocumentId",i.getSourceDocumentId(),"guideVersionId",i.getGuideVersionId(),
   "metadata",payloads.values(source),"searchMode","TEXT","embeddingsAvailable",false,"verificationStatus","VALIDATED");
 }
}
