package com.orientation.orientationapp.intelligence.indexing;
import com.orientation.orientationapp.intelligence.extraction.EntityExtractionService.ExtractedEntities;
import com.orientation.orientationapp.knowledge.entity.*;
import com.orientation.orientationapp.knowledge.repository.*;
import com.orientation.orientationapp.modules.university.service.CatalogAccess;
import com.orientation.orientationapp.security.service.ResourceAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service @RequiredArgsConstructor
public class SearchIndexService {
 private final SearchIndexRepository indices;
 private final KnowledgeNodeRepository nodes;
 private final CatalogAccess scope;
 private final ResourceAccess access;
 @Transactional public void index(KnowledgeNode node) {
  if(!Boolean.TRUE.equals(node.getVerified()) || node.getGuideVersionId()==null)throw new IllegalArgumentException("Only verified versioned knowledge can be indexed");
  var index=indices.findByEntityTypeAndEntityIdAndGuideVersionId(node.getEntityType(),node.getEntityId(),node.getGuideVersionId()).orElseGet(SearchIndex::new);
  index.setNodeId(node.getId());index.setNodeType(node.getNodeType());index.setProgramId(node.getProgramId());index.setUniversityId(node.getUniversityId());
  index.setEntityType(node.getEntityType());index.setEntityId(node.getEntityId());index.setName(node.getName());
  index.setContent(node.getDescription());index.setMetadata(node.getProperties());index.setEmbedding(null);
  index.setTenantId(node.getTenantId());index.setGuideVersionId(node.getGuideVersionId());
  index.setCountryId(node.getCountryId());index.setAcademicYearId(node.getAcademicYearId());index.setSourceDocumentId(node.getSourceDocumentId());
  index.setVerified(true);index.setActive(node.getActive());indices.saveAndFlush(index);
 }
 @Transactional public int rebuild() {
  access.require("GUIDE_PUBLISH");UUID tenant=scope.tenant();boolean global=scope.global();
  var verified=nodes.findAll((root,q,cb)->cb.and(cb.isTrue(root.get("verified")),
   global?cb.conjunction():cb.equal(root.get("tenantId"),tenant)));
  verified.forEach(this::index);return verified.size();
 }
 @Deprecated public void reindex(ExtractedEntities ignored,UUID id) {
  throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT,"Validate and publish source facts before indexing");
 }
}
