package com.orientation.orientationapp.modules.orientation.controller;
import com.orientation.orientationapp.modules.orientation.service.AdmissionRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import java.util.*;
@RestController @RequiredArgsConstructor
public class AdmissionCriterionController {
 private final AdmissionRuleService rules;
 @GetMapping("/admission-criteria") public Page<Map<String,Object>> list(@RequestParam(required=false) UUID programId,
  @RequestParam(required=false) UUID guideVersionId,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
  return rules.list(programId,guideVersionId,page,size);
 }
 @GetMapping("/admission-criteria/{id}") public Map<String,Object> get(@PathVariable UUID id){return rules.get(id);}
 @GetMapping("/scholarships/{id}/criteria") public List<Map<String,Object>> scholarships(@PathVariable UUID id){return rules.scholarshipRules(id);}
}
