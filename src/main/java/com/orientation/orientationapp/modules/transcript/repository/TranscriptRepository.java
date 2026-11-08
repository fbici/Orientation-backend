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
    org.springframework.data.domain.Page<Transcript> findByCandidateId(UUID id,org.springframework.data.domain.Pageable pageable);
    @org.springframework.data.jpa.repository.Query("select t from Transcript t where t.candidate.tenant.id=:tenant")
    org.springframework.data.domain.Page<Transcript> findScoped(@org.springframework.data.repository.query.Param("tenant") UUID tenant,org.springframework.data.domain.Pageable pageable);

    List<Transcript> findByCandidateId(UUID candidateId);
    List<Transcript> findByAcademicYearId(UUID academicYearId);
    List<Transcript> findByStatus(TranscriptStatus status);
}
