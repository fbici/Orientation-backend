package com.orientation.orientationapp.modules.transcript.controller;
import com.orientation.orientationapp.modules.transcript.service.TranscriptAnalysisService;
import com.orientation.orientationapp.modules.transcript.dto.TranscriptReview;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import java.util.UUID;
@RestController @RequestMapping("/transcripts/{id}/analyses") @RequiredArgsConstructor
public class TranscriptAnalysisController {
    private final TranscriptAnalysisService service;
    @PostMapping public TranscriptAnalysisService.View analyze(@PathVariable UUID id,@Valid @RequestBody TranscriptAnalysisService.Analyze request){return service.analyze(id,request);}
    @GetMapping public Page<TranscriptAnalysisService.View> history(@PathVariable UUID id,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.history(id,page,size);}
    @PutMapping("/{analysisId}") public TranscriptAnalysisService.View correct(@PathVariable UUID id,@PathVariable UUID analysisId,@Valid @RequestBody TranscriptReview request){return service.correct(id,analysisId,request);}
    @PostMapping("/{analysisId}/confirm") public TranscriptAnalysisService.View confirm(@PathVariable UUID id,@PathVariable UUID analysisId){return service.confirm(id,analysisId);}
}
