package com.orientation.orientationapp.knowledge.smartquery.service.impl;
import com.orientation.orientationapp.knowledge.smartquery.service.SmartQueryService;
import com.orientation.orientationapp.knowledge.search.service.SemanticSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
@Service @RequiredArgsConstructor
public class DefaultSmartQueryService implements SmartQueryService {
 private final SemanticSearchService search;
 private static final Set<String> STOP=Set.of("les","des","une","quels","quelles","quelle","quel","pour","avec","dans","sont","est","the","and","what","which","universites","programmes");
 public Map<String,Object> processQuery(String query) {
  if(query==null || query.isBlank() || query.length()>200)throw com.orientation.orientationapp.modules.university.service.AcademicCatalogService.invalid("Enter a query of 1 to 200 characters");
  var matches=new LinkedHashMap<String,Map<String,Object>>();
  for(var item:search.search(query,20))matches.put(String.valueOf(item.get("indexId")),item);
  if(matches.isEmpty()) {
   var words=Arrays.stream(query.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+")).filter(w->w.length()>=3 && !STOP.contains(w)).distinct().limit(8).toList();
   for(String word:words)for(var item:search.search(word,10))matches.putIfAbsent(String.valueOf(item.get("indexId")),item);
  }
  var results=matches.values().stream().limit(20).toList();
  return Map.of("query",query,"searchMode","TEXT","embeddingsAvailable",false,"results",results,
   "response",results.isEmpty()?"Aucune information validee ne correspond a cette recherche.":"Informations documentees retrouvees. Consultez les passages sources ; aucune admission ni bourse n'est garantie.");
 }
 public Map<String,Object> getSuggestions(String partial) {
  if(partial==null || partial.isBlank())return Map.of("suggestions",List.of());
  return Map.of("suggestions",search.search(partial,5).stream().map(r->r.get("name")).distinct().toList());
 }
}
