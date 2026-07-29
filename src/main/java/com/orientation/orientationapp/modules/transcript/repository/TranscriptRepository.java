package com.orientation.orientationapp.modules.transcript.repository;

import com.orientation.orientationapp.common.enums.TranscriptStatus;
import com.orientation.orientationapp.modules.transcript.entity.Transcript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TranscriptRepository extends JpaRepository<Transcript, UUID> {
    List<Transcript> findByCandidateId(UUID candidateId);
    List<Transcript> findByAcademicYearId(UUID academicYearId);
    Optional<Transcript> findByCandidateIdAndAcademicYearId(UUID candidateId, UUID academicYearId);
    List<Transcript> findByStatus(TranscriptStatus status);
}
