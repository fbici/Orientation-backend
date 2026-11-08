package com.orientation.orientationapp.modules.transcript.controller;
import com.orientation.orientationapp.modules.transcript.dto.TranscriptResponse;
import com.orientation.orientationapp.modules.transcript.service.TranscriptAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;
@RestController @RequestMapping("/transcripts") @RequiredArgsConstructor
public class TranscriptController {
    private final TranscriptAccessService service;
    @GetMapping public ResponseEntity<Page<TranscriptResponse>> list(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(service.list(PageRequest.of(Math.max(0,page),Math.max(1,Math.min(size,100)))).map(TranscriptResponse::from));
    }
    @GetMapping("/{id}") public ResponseEntity<TranscriptResponse> getById(@PathVariable UUID id) { return ResponseEntity.ok(TranscriptResponse.from(service.get(id))); }
    @PostMapping public ResponseEntity<TranscriptResponse> upload(@RequestParam("file") MultipartFile file,@RequestParam(required=false) UUID academicYearId) { return ResponseEntity.ok(TranscriptResponse.from(service.upload(file,academicYearId))); }
}
