package com.orientation.orientationapp.knowledge.repository;

import com.orientation.orientationapp.knowledge.entity.KnowledgeRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KnowledgeRelationRepository extends JpaRepository<KnowledgeRelation, UUID> {
    List<KnowledgeRelation> findBySourceId(UUID sourceId);
    List<KnowledgeRelation> findByTargetId(UUID targetId);
    List<KnowledgeRelation> findByRelationType(String relationType);
    List<KnowledgeRelation> findBySourceIdAndRelationType(UUID sourceId, String relationType);
}
