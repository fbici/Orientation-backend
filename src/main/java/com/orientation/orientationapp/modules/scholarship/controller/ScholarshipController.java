package com.orientation.orientationapp.modules.scholarship.controller;

import com.orientation.orientationapp.modules.scholarship.entity.Scholarship;
import com.orientation.orientationapp.modules.scholarship.repository.ScholarshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * API REST pour les bourses.
 *
 * GET /api/v1/scholarships - Liste paginée
 * GET /api/v1/scholarships/{id} - Détail
 */
@RestController
@RequestMapping("/scholarships")
@RequiredArgsConstructor
public class ScholarshipController {

    private final ScholarshipRepository scholarshipRepository;

    @GetMapping
    public ResponseEntity<Page<Scholarship>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(scholarshipRepository.findAll(
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Scholarship> getById(@PathVariable UUID id) {
        return scholarshipRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
