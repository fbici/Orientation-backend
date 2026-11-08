package com.orientation.orientationapp.recengine.recommendation.controller;
import com.orientation.orientationapp.modules.recommendation.service.PersistedRecommendationService;
import com.orientation.orientationapp.recengine.recommendation.dto.request.GenerateRecommendationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

@RestController @RequestMapping("/recommendations") @RequiredArgsConstructor
public class RecommendationController {
    private final PersistedRecommendationService service;
    private final com.orientation.orientationapp.recengine.simulation.engine.impl.DefaultSimulationEngine simulations;
    @PostMapping("/generate")
    public PersistedRecommendationService.Generation generate(@Valid @RequestBody GenerateRecommendationRequest request){return service.generate(request);}
    @GetMapping
    public Page<PersistedRecommendationService.Item> list(@RequestParam(required=false) UUID candidateId,
        @RequestParam(required=false) UUID runId,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        return service.list(candidateId,runId,page,size);
    }
    @GetMapping("/history")
    public Page<PersistedRecommendationService.RunView> history(@RequestParam(required=false) UUID candidateId,
        @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.history(candidateId,page,size);}
    @GetMapping("/{id}") public PersistedRecommendationService.Item get(@PathVariable UUID id){return service.get(id);}
    @GetMapping("/{id}/scores") public com.fasterxml.jackson.databind.JsonNode scores(@PathVariable UUID id){return service.get(id).explanation().get("scoring");}
    @GetMapping("/{id}/explanation") public com.fasterxml.jackson.databind.JsonNode explanation(@PathVariable UUID id){return service.get(id).explanation();}
    @PostMapping("/simulate") public com.orientation.orientationapp.ai.history.DecisionHistoryService.View simulate(
        @Valid @RequestBody com.orientation.orientationapp.recengine.recommendation.dto.request.SimulationRequest request) {
        return simulations.simulate(request);
    }
}
