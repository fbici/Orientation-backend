package com.orientation.orientationapp.modules.user.controller;
import com.orientation.orientationapp.modules.user.dto.CandidateResponse;
import com.orientation.orientationapp.modules.user.service.CandidateAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.*;
@RestController @RequestMapping("/candidates") @RequiredArgsConstructor
public class CandidateController {
    private final CandidateAccessService service;
    @GetMapping @PreAuthorize("hasAuthority('CANDIDATE_READ')")
    public ResponseEntity<Page<CandidateResponse>> list(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(service.list(null,PageRequest.of(Math.max(0,page),Math.max(1,Math.min(size,100)))).map(CandidateResponse::from));
    }
    @GetMapping("/{id}")
    public ResponseEntity<CandidateResponse> getById(@PathVariable UUID id) { return ResponseEntity.ok(CandidateResponse.from(service.get(id))); }
    @GetMapping("/search") @PreAuthorize("hasAuthority('CANDIDATE_READ')")
    public ResponseEntity<java.util.List<CandidateResponse>> search(@RequestParam String q) {
        return ResponseEntity.ok(service.list(q,PageRequest.of(0,50)).map(CandidateResponse::from).getContent());
    }
    @GetMapping("/count") @PreAuthorize("hasAuthority('CANDIDATE_READ')")
    public ResponseEntity<Map<String,Object>> count() {
        return ResponseEntity.ok(Map.of("total",service.list(null,PageRequest.of(0,1)).getTotalElements()));
    }
}
