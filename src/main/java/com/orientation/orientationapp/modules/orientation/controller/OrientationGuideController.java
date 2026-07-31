package com.orientation.orientationapp.modules.orientation.controller;

import com.orientation.orientationapp.modules.orientation.entity.OrientationGuide;
import com.orientation.orientationapp.modules.orientation.repository.OrientationGuideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * API REST pour les guides d'orientation.
 *
 * GET /api/v1/guides - Liste paginée
 * GET /api/v1/guides/{id} - Détail
 */
@RestController
@RequestMapping("/guides")
@RequiredArgsConstructor
public class OrientationGuideController {

    private final OrientationGuideRepository guideRepository;

    @GetMapping
    public ResponseEntity<Page<OrientationGuide>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(guideRepository.findAll(
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrientationGuide> getById(@PathVariable UUID id) {
        return guideRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
