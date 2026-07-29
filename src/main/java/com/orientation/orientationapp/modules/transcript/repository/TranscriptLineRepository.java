package com.orientation.orientationapp.modules.transcript.repository;

import com.orientation.orientationapp.modules.transcript.entity.TranscriptLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TranscriptLineRepository extends JpaRepository<TranscriptLine, UUID> {
    List<TranscriptLine> findByTranscriptId(UUID transcriptId);
    List<TranscriptLine> findByTranscriptIdAndSemester(UUID transcriptId, Integer semester);
}
