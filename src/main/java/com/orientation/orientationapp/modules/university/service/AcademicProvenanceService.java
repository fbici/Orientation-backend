package com.orientation.orientationapp.modules.university.service;
import com.orientation.orientationapp.intelligence.knowledge.KnowledgeGraphService;
import com.orientation.orientationapp.knowledge.repository.KnowledgeNodeRepository;
import com.orientation.orientationapp.modules.orientation.repository.GuideVersionRepository;
import com.orientation.orientationapp.common.enums.GuideVersionStatus;
import com.orientation.orientationapp.docintel.document.service.FactPayloadMapper;
import com.orientation.orientationapp.docintel.document.entity.ExtractedFact;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.time.LocalDate;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class AcademicProvenanceService {
 private final AcademicCatalogService catalogue;private final KnowledgeGraphService graph;
 private final KnowledgeNodeRepository nodes;private final FactPayloadMapper payloads;private final GuideVersionRepository versions;
 public List<Object> sources(String resource,UUID id,UUID guideVersionId,boolean currentOnly) {
  catalogue.get(resource,id);var result=new LinkedHashMap<String,Object>();var now=LocalDate.now();
  for(var n:nodes.findAll(graph.visible().and((r,q,cb)->cb.equal(r.get("entityId"),id)))) {
   if(guideVersionId!=null&&!guideVersionId.equals(n.getGuideVersionId()))continue;
   if(currentOnly) {
    var version=n.getGuideVersionId()==null?null:versions.findById(n.getGuideVersionId()).orElse(null);
    if(version==null||version.getStatus()!=GuideVersionStatus.ACTIVE||!Boolean.TRUE.equals(version.getActive())||
       version.getEffectiveDate()==null||version.getEffectiveDate().isAfter(now)||
       version.getExpiryDate()!=null&&version.getExpiryDate().isBefore(now))continue;
   }
   var fact=new ExtractedFact();fact.setPayload(n.getProperties());var raw=payloads.values(fact).get("sources");
   if(raw instanceof List<?> list)for(var item:list)if(item instanceof Map<?,?> source)
    result.put(String.valueOf(source.get("factId")),source);
  }
  return List.copyOf(result.values());
 }
}
