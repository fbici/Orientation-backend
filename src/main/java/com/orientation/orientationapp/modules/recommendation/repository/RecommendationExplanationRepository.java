package com.orientation.orientationapp.modules.recommendation.repository;

import com.orientation.orientationapp.modules.recommendation.entity.RecommendationExplanation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecommendationExplanationRepository extends JpaRepository<RecommendationExplanation, UUID> {
    List<RecommendationExplanation> findByRecommendationId(UUID recommendationId);
}
