package com.orientation.orientationapp.modules.user.controller;

import com.orientation.orientationapp.modules.user.dto.AcademicProfileRequest;
import com.orientation.orientationapp.modules.user.service.CandidateAcademicService;
import com.orientation.orientationapp.security.service.ResourceAccess;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController @RequestMapping("/candidates") @RequiredArgsConstructor
public class CandidateAcademicController {
    private final CandidateAcademicService service;
    private final ResourceAccess access;
    @GetMapping("/me/academic-profile")
    public CandidateAcademicService.View mine(){return service.read(access.ownCandidate().getId());}
    @PutMapping("/me/academic-profile")
    public CandidateAcademicService.View saveMine(@Valid @RequestBody AcademicProfileRequest request) {
        return service.save(access.ownCandidate().getId(),request);
    }
    @GetMapping("/{id}/academic-profile")
    public CandidateAcademicService.View get(@PathVariable UUID id){return service.read(id);}
    @PutMapping("/{id}/academic-profile")
    public CandidateAcademicService.View save(@PathVariable UUID id,@Valid @RequestBody AcademicProfileRequest request){
        return service.save(id,request);
    }
}
