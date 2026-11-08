package com.orientation.orientationapp.modules.scholarship.controller;
import com.orientation.orientationapp.modules.scholarship.service.ScholarshipRecommendationService;
import com.orientation.orientationapp.modules.scholarship.dto.ScholarshipEvaluationRequest;
import com.orientation.orientationapp.ai.history.DecisionHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import java.util.*;
@RestController @RequestMapping("/scholarships") @RequiredArgsConstructor
public class ScholarshipEvaluationController {
 private final ScholarshipRecommendationService service;private final DecisionHistoryService history;
 @PostMapping("/evaluate") public DecisionHistoryService.View evaluate(@Valid @RequestBody ScholarshipEvaluationRequest request){return service.evaluate(request);}
 @PostMapping("/{id}/evaluate") public DecisionHistoryService.View evaluateOne(@PathVariable UUID id,@RequestParam(required=false) UUID candidateId,@RequestParam(required=false) UUID programId){
  return service.evaluate(new ScholarshipEvaluationRequest(candidateId,programId,List.of(id)));
 }
 @GetMapping("/evaluations") public Page<DecisionHistoryService.View> history(@RequestParam(required=false) UUID candidateId,
  @RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){return history.history(candidateId,"SCHOLARSHIP",page,size);}
}
