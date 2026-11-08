package com.orientation.orientationapp.intelligence.smartquery;
import com.orientation.orientationapp.knowledge.smartquery.service.SmartQueryService;
import lombok.*;
import org.springframework.stereotype.Service;
import java.util.*;
/** Compatibility adapter to the existing, now source-backed SQL search service. */
@Service @RequiredArgsConstructor
public class SmartQueryEngine {
 private final SmartQueryService search;
 @SuppressWarnings("unchecked")
 public SmartQueryResult query(String question) {
  Map<String,Object> response=search.processQuery(question);
  var nodes=(List<Map<String,Object>>)response.get("results");
  var groups=new LinkedHashMap<String,List<Map<String,Object>>>();var sources=new LinkedHashMap<String,Map<String,Object>>();
  for(var node:nodes) {
   groups.computeIfAbsent(String.valueOf(node.get("type")),key->new ArrayList<>()).add(node);
   if(node.get("metadata") instanceof Map<?,?> metadata && metadata.get("sources") instanceof List<?> evidence)
    for(var item:evidence)if(item instanceof Map<?,?> source)sources.put(String.valueOf(source.get("factId")),(Map<String,Object>)source);
  }
  return SmartQueryResult.builder().question(question).keywords(Arrays.asList(question.split("\\s+"))).knowledgeNodes(nodes).groupedNodes(groups)
   .answer(String.valueOf(response.get("response"))).recommendedPrograms(groups.getOrDefault("PROGRAM",List.of()).stream().map(n->String.valueOf(n.get("name"))).toList())
   .sources(new ArrayList<>(sources.values())).searchMode("TEXT").embeddingsAvailable(false).build();
 }
 @Data @Builder
 public static class SmartQueryResult {
  private String question;
  private List<String> keywords;
  private List<Map<String,Object>> knowledgeNodes;
  private Map<String,List<Map<String,Object>>> groupedNodes;
  private String answer;
  private List<String> recommendedPrograms;
  private List<Map<String,Object>> sources;
  private String searchMode;
  private boolean embeddingsAvailable;
 }
}
