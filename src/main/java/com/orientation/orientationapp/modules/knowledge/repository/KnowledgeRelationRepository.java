package com.orientation.orientationapp.modules.knowledge.repository;

import com.orientation.orientationapp.modules.knowledge.entity.KnowledgeRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Repository pour les relations du Knowledge Graph.
 */
@Repository
public interface KnowledgeRelationRepository extends JpaRepository<KnowledgeRelation, UUID> {

    /**
     * Trouve toutes les relations où un nœud est source ou cible.
     */
    List<KnowledgeRelation> findBySourceIdOrTargetId(UUID sourceId, UUID targetId);

    /**
     * Trouve les relations par type.
     */
    List<KnowledgeRelation> findByType(String type);

    /**
     * Vérifie si une relation existe entre deux nœuds.
     */
    boolean existsBySourceIdAndTargetIdAndType(UUID sourceId, UUID targetId, String type);

    /**
     * Trouve les relations sortantes d'un nœud.
     */
    List<KnowledgeRelation> findBySourceId(UUID sourceId);

    /**
     * Trouve les relations entrantes d'un nœud.
     */
    List<KnowledgeRelation> findByTargetId(UUID targetId);
}
