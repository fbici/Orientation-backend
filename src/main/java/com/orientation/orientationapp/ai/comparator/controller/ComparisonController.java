package com.orientation.orientationapp.ai.comparator.controller;

import com.orientation.orientationapp.ai.comparator.model.ComparisonResult;
import com.orientation.orientationapp.ai.comparator.service.ComparisonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/ai/compare")
@RequiredArgsConstructor
public class ComparisonController {

    private final ComparisonService comparisonService;

    @GetMapping("/programs")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT')")
    public ResponseEntity<ComparisonResult> comparePrograms(
            @RequestParam UUID programA,
            @RequestParam UUID programB) {
        return ResponseEntity.ok(comparisonService.comparePrograms(programA, programB));
    }

    @GetMapping("/universities")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT')")
    public ResponseEntity<ComparisonResult> compareUniversities(
            @RequestParam UUID universityA,
            @RequestParam UUID universityB) {
        return ResponseEntity.ok(comparisonService.compareUniversities(universityA, universityB));
    }
}
