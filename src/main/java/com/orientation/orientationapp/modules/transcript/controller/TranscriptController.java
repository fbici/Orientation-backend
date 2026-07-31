package com.orientation.orientationapp.modules.transcript.controller;

import com.orientation.orientationapp.common.enums.TranscriptStatus;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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

        // Sauvegarder le fichier sur disque
        String uploadDir = "./uploads/transcripts/";
        String savedPath = uploadDir + UUID.randomUUID() + "_" + file.getOriginalFilename();
        try {
            Files.createDirectories(Paths.get(uploadDir));
            file.transferTo(Paths.get(savedPath));
        } catch (Exception e) {
            log.error("Failed to save file", e);
            return ResponseEntity.internalServerError().build();
        }

        Transcript transcript = new Transcript();
        transcript.setOriginalFileName(file.getOriginalFilename());
        transcript.setFileUrl(savedPath);
        transcript.setTitle(file.getOriginalFilename());
        transcript.setStatus(TranscriptStatus.DRAFT);
        transcript.setSource("UPLOAD");

        transcript = transcriptRepository.save(transcript);
        log.info("Transcript saved: {}", transcript.getId());
        return ResponseEntity.ok(transcript);
    }
}
