package com.orientation.orientationapp.modules.transcript.controller;

import com.orientation.orientationapp.modules.transcript.entity.Transcript;
import com.orientation.orientationapp.modules.transcript.repository.TranscriptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * API REST pour les relevés de notes.
 *
 * GET  /api/v1/transcripts          - Liste paginée
 * GET  /api/v1/transcripts/{id}     - Détail
 * POST /api/v1/transcripts          - Upload d'un relevé
 */
@Slf4j
@RestController
@RequestMapping("/transcripts")
@RequiredArgsConstructor
public class TranscriptController {

    private final TranscriptRepository transcriptRepository;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Page<Transcript>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(transcriptRepository.findAll(
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT')")
    public ResponseEntity<Transcript> getById(@PathVariable UUID id) {
        return transcriptRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT')")
    public ResponseEntity<Transcript> upload(@RequestParam("file") MultipartFile file) {
        log.info("Transcript upload: {} ({} bytes)", file.getOriginalFilename(), file.getSize());

        Transcript transcript = new Transcript();
        transcript.setFileName(file.getOriginalFilename());
        transcript.setStatus(com.orientation.orientationapp.common.enums.TranscriptStatus.UPLOADED);

        try {
            transcript.setContent(file.getBytes());
        } catch (Exception e) {
            log.error("Failed to read file content", e);
        }

        transcript = transcriptRepository.save(transcript);
        return ResponseEntity.ok(transcript);
    }
}
