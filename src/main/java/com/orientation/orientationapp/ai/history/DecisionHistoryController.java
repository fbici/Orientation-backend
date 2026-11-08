package com.orientation.orientationapp.ai.history;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import java.util.UUID;
@RestController @RequestMapping("/ai/decisions") @RequiredArgsConstructor
public class DecisionHistoryController {
 private final DecisionHistoryService service;
 @GetMapping public Page<DecisionHistoryService.View> history(@RequestParam(required=false)UUID candidateId,@RequestParam(required=false)String kind,
  @RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){return service.history(candidateId,kind,page,size);}
 @GetMapping("/{id}") public DecisionHistoryService.View get(@PathVariable UUID id){return service.get(id);}
}
