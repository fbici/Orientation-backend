package com.orientation.orientationapp.knowledge.repository;

import com.orientation.orientationapp.knowledge.entity.KnowledgeNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KnowledgeNodeRepository extends JpaRepository<KnowledgeNode, UUID> {
    List<KnowledgeNode> findByNodeType(String nodeType);
    List<KnowledgeNode> findByEntityType(String entityType);
    List<KnowledgeNode> findByEntityTypeAndEntityId(String entityType, UUID entityId);
    List<KnowledgeNode> findByNameContainingIgnoreCase(String name);

    @Query("SELECT kn FROM KnowledgeNode kn WHERE kn.active = true AND kn.nodeType = :nodeType")
    List<KnowledgeNode> findActiveByNodeType(@Param("nodeType") String nodeType);
}
