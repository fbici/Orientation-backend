package com.orientation.orientationapp.knowledge.repository;

import com.orientation.orientationapp.knowledge.entity.SimilarityMatrix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SimilarityMatrixRepository extends JpaRepository<SimilarityMatrix, UUID> {
    List<SimilarityMatrix> findByEntityTypeAndEntityIdA(String entityType, UUID entityId);
    List<SimilarityMatrix> findByEntityTypeAndEntityIdB(String entityType, UUID entityId);
    List<SimilarityMatrix> findByEntityTypeAndActiveTrue(String entityType);
}
