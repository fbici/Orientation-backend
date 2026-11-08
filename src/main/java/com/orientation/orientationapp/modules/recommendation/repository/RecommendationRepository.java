package com.orientation.orientationapp.modules.recommendation.repository;

import com.orientation.orientationapp.common.enums.RecommendationStatus;
import com.orientation.orientationapp.modules.recommendation.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, UUID> {
    org.springframework.data.domain.Page<Recommendation> findByCandidateIdAndRunIdIsNotNullOrderByRecommendedAtDescRankAsc(UUID candidateId,org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<Recommendation> findByCandidateIdAndRunIdOrderByRankAsc(UUID candidateId,UUID runId,org.springframework.data.domain.Pageable pageable);
    List<Recommendation> findByCandidateId(UUID candidateId);
    List<Recommendation> findByCandidateIdAndGuideVersionId(UUID candidateId, UUID guideVersionId);
    List<Recommendation> findByProgramId(UUID programId);
    List<Recommendation> findByStatus(RecommendationStatus status);
}
