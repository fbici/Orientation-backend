package com.orientation.orientationapp.modules.transcript.repository;

import com.orientation.orientationapp.modules.transcript.entity.TranscriptAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TranscriptAnalysisRepository extends JpaRepository<TranscriptAnalysis, UUID> {
    Optional<TranscriptAnalysis> findByParentAnalysisIdAndConfirmedAtIsNotNull(UUID parentId);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select a from TranscriptAnalysis a where a.id=:id")
    Optional<TranscriptAnalysis> lockById(@org.springframework.data.repository.query.Param("id") UUID id);
    org.springframework.data.domain.Page<TranscriptAnalysis> findByTranscriptIdOrderByAnalyzedAtDesc(UUID transcriptId,org.springframework.data.domain.Pageable pageable);
}
