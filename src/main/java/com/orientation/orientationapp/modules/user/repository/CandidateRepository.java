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
    java.util.Optional<Candidate> findByUserId(UUID id);
    @org.springframework.data.jpa.repository.Query("select c from Candidate c where c.tenant.id=:tenant and c.user is not null and (:search is null or lower(c.email) like lower(concat('%',:search,'%')) or lower(c.firstName) like lower(concat('%',:search,'%')) or lower(c.lastName) like lower(concat('%',:search,'%')))")
    org.springframework.data.domain.Page<Candidate> findScoped(@org.springframework.data.repository.query.Param("tenant") UUID tenant,@org.springframework.data.repository.query.Param("search") String search,org.springframework.data.domain.Pageable pageable);

    long countByTenant_IdAndUserIsNotNull(UUID tenantId);
    Optional<Candidate> findByEmail(String email);
    List<Candidate> findByCountryId(UUID countryId);
    List<Candidate> findByStatus(CandidateStatus status);
    List<Candidate> findByBacYear(Integer bacYear);
    boolean existsByEmail(String email);
}
