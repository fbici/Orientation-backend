package com.orientation.orientationapp.modules.user.controller;

import com.orientation.orientationapp.modules.user.entity.Candidate;
import com.orientation.orientationapp.modules.user.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * API REST pour les candidats.
 *
 * GET    /api/v1/candidates          - Liste des candidats (paginée)
 * GET    /api/v1/candidates/{id}     - Détail d'un candidat
 * GET    /api/v1/candidates/search   - Recherche par nom/email
 */
@Slf4j
@RestController
@RequestMapping("/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateRepository candidateRepository;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Page<Candidate>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Candidate> result = candidateRepository.findAll(
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT')")
    public ResponseEntity<Candidate> getById(@PathVariable UUID id) {
        return candidateRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<List<Candidate>> search(@RequestParam String q) {
        List<Candidate> results = candidateRepository.findAll().stream()
                .filter(c -> (c.getFirstName() + " " + c.getLastName()).toLowerCase().contains(q.toLowerCase())
                        || (c.getEmail() != null && c.getEmail().toLowerCase().contains(q.toLowerCase())))
                .limit(50)
                .toList();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> count() {
        long total = candidateRepository.count();
        long active = candidateRepository.findAll().stream()
                .filter(c -> c.getStatus() != null && c.getStatus().name().equals("ACTIVE"))
                .count();
        return ResponseEntity.ok(Map.of("total", total, "active", active));
    }
}
