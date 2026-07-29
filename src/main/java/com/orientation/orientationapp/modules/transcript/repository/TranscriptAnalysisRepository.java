package com.orientation.orientationapp.modules.transcript.repository;

import com.orientation.orientationapp.modules.transcript.entity.TranscriptAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TranscriptAnalysisRepository extends JpaRepository<TranscriptAnalysis, UUID> {
    Optional<TranscriptAnalysis> findByTranscriptId(UUID transcriptId);
}
