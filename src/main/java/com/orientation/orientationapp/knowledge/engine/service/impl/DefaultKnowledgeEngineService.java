package com.orientation.orientationapp.knowledge.engine.service.impl;
import com.orientation.orientationapp.knowledge.engine.service.KnowledgeEngineService;
import com.orientation.orientationapp.intelligence.indexing.SearchIndexService;
import com.orientation.orientationapp.intelligence.knowledge.KnowledgeGraphService;
import com.orientation.orientationapp.knowledge.repository.KnowledgeNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class DefaultKnowledgeEngineService implements KnowledgeEngineService {
 private final KnowledgeGraphService graph;
 private final SearchIndexService index;
 private final KnowledgeNodeRepository nodes;
 @Override @Transactional public void rebuildIndex(){index.rebuild();}
 @Override public Map<String,Object> getStatistics() {
  var counts=new LinkedHashMap<String,Long>();
  for(String type:List.of("UNIVERSITY","CAMPUS","FACULTY","PROGRAM","SCHOLARSHIP","ADMISSION_CRITERION","SCHOLARSHIP_CRITERION","GUIDE_VERSION","ACADEMIC_FACT"))
   counts.put(type,nodes.count(graph.visible().and((r,q,cb)->cb.equal(r.get("entityType"),type))));
  return Map.of("totalNodes",nodes.count(graph.visible()),"nodeTypes",counts,"searchMode","TEXT","embeddingsAvailable",false);
 }
 @Override public Map<String,Object> getNode(String type,UUID id) {
  return nodes.findAll(graph.visible().and((r,q,cb)->cb.and(cb.equal(r.get("entityType"),type),cb.equal(r.get("entityId"),id))),
   org.springframework.data.domain.PageRequest.of(0,1)).stream().findFirst().map(graph::view).orElse(Map.of());
 }
 @Override public List<Map<String,Object>> getNodesByType(String type) {
  return nodes.findAll(graph.visible().and((r,q,cb)->cb.equal(r.get("nodeType"),type)),org.springframework.data.domain.PageRequest.of(0,100))
   .stream().map(graph::view).toList();
 }
}
