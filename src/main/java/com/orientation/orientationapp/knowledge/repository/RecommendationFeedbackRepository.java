package com.orientation.orientationapp.knowledge.repository;

import com.orientation.orientationapp.knowledge.entity.RecommendationFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecommendationFeedbackRepository extends JpaRepository<RecommendationFeedback, UUID> {
    List<RecommendationFeedback> findByCandidateId(UUID candidateId);
    List<RecommendationFeedback> findByProgramId(UUID programId);
    long countByProgramIdAndFeedbackType(UUID programId, RecommendationFeedback.FeedbackType type);
}
