package com.orientation.orientationapp.knowledge.repository;

import com.orientation.orientationapp.knowledge.entity.LearningHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LearningHistoryRepository extends JpaRepository<LearningHistory, UUID> {
    List<LearningHistory> findByCandidateId(UUID candidateId);
    List<LearningHistory> findByProgramId(UUID programId);
    long countByProgramIdAndEvent(UUID programId, LearningHistory.LearningEvent event);
}
