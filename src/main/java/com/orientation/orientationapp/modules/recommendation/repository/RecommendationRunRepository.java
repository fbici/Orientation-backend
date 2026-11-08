package com.orientation.orientationapp.modules.recommendation.repository;
import com.orientation.orientationapp.modules.recommendation.entity.RecommendationRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
import java.util.UUID;
public interface RecommendationRunRepository extends JpaRepository<RecommendationRun,UUID> {
    Page<RecommendationRun> findByCandidateIdOrderByComputedAtDesc(UUID id,Pageable pageable);
}
