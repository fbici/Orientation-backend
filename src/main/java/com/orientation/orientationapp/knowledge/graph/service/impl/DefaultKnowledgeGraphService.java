package com.orientation.orientationapp.knowledge.graph.service.impl;
import com.orientation.orientationapp.knowledge.graph.service.KnowledgeGraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
@Service @RequiredArgsConstructor
public class DefaultKnowledgeGraphService implements KnowledgeGraphService {
 private final com.orientation.orientationapp.intelligence.knowledge.KnowledgeGraphService graph;
 @Override public Map<String,Object> getGraph(){return graph.graph();}
 @SuppressWarnings("unchecked")
 private List<Map<String,Object>> nodes(Map<String,Object> data){return (List<Map<String,Object>>)data.get("nodes");}
 @SuppressWarnings("unchecked")
 private List<Map<String,Object>> edges(Map<String,Object> data){return (List<Map<String,Object>>)data.get("edges");}
 private Set<UUID> matching(List<Map<String,Object>> nodes,String type,UUID id) {
  var ids=new HashSet<UUID>();for(var n:nodes)if(type.equals(n.get("type")) && id.equals(n.get("entityId")))ids.add((UUID)n.get("id"));return ids;
 }
 @Override public List<Map<String,Object>> getRelatedEntities(String type,UUID id,int depth) {
  if(depth<1 || depth>5)throw com.orientation.orientationapp.modules.university.service.AcademicCatalogService.invalid("Graph depth must be 1 to 5");
  var data=graph.graph();var all=nodes(data);var edges=edges(data);var visited=matching(all,type,id);
  var frontier=new HashSet<>(visited);var result=new HashSet<UUID>();
  for(int i=0;i<depth;i++) {
   var next=new HashSet<UUID>();
   for(var e:edges)if(frontier.contains(e.get("source")) && visited.add((UUID)e.get("target")))next.add((UUID)e.get("target"));
   result.addAll(next);frontier=next;
  }
  return all.stream().filter(n->result.contains(n.get("id"))).toList();
 }
 @Override public List<Map<String,Object>> findPath(String fromType,UUID fromId,String toType,UUID toId) {
  var data=graph.graph();var all=nodes(data);var links=edges(data);
  var start=matching(all,fromType,fromId);var target=matching(all,toType,toId);
  var queue=new ArrayDeque<UUID>(start);var parent=new HashMap<UUID,UUID>();start.forEach(id->parent.put(id,null));
  UUID end=null;
  while(!queue.isEmpty()) {
   UUID current=queue.remove();if(target.contains(current)){end=current;break;}
   for(var link:links)if(current.equals(link.get("source"))) {
    UUID next=(UUID)link.get("target");if(!parent.containsKey(next)){parent.put(next,current);queue.add(next);}
   }
  }
  if(end==null)return List.of();
  var order=new ArrayList<UUID>();for(UUID current=end;current!=null;current=parent.get(current))order.add(current);Collections.reverse(order);
  var map=new HashMap<UUID,Map<String,Object>>();all.forEach(n->map.put((UUID)n.get("id"),n));
  return order.stream().map(map::get).toList();
 }
}
