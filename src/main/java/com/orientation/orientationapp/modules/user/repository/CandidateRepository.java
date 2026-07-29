package com.orientation.orientationapp.modules.user.repository;

import com.orientation.orientationapp.common.enums.CandidateStatus;
import com.orientation.orientationapp.modules.user.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, UUID> {
    Optional<Candidate> findByEmail(String email);
    List<Candidate> findByCountryId(UUID countryId);
    List<Candidate> findByStatus(CandidateStatus status);
    List<Candidate> findByBacYear(Integer bacYear);
    boolean existsByEmail(String email);
}
