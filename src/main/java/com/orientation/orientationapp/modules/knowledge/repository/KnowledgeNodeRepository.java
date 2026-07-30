package com.orientation.orientationapp.modules.knowledge.repository;

import com.orientation.orientationapp.modules.knowledge.entity.KnowledgeNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Repository pour les nœuds du Knowledge Graph.
 */
@Repository
public interface KnowledgeNodeRepository extends JpaRepository<KnowledgeNode, UUID> {

    /**
     * Trouve un nœud par type et nom.
     */
    Optional<KnowledgeNode> findByTypeAndName(String type, String name);

    /**
     * Recherche par nom (insensible à la casse).
     */
    List<KnowledgeNode> findByNameContainingIgnoreCase(String name);

    /**
     * Trouve tous les nœuds d'un type donné.
     */
    List<KnowledgeNode> findByType(String type);

    /**
     * Trouve les nœuds actifs d'un type donné.
     */
    List<KnowledgeNode> findByTypeAndActiveTrue(String type);

    /**
     * Compte les nœuds par type.
     */
    @Query("SELECT n.type, COUNT(n) FROM KnowledgeNode n GROUP BY n.type")
    List<Object[]> countByType();

    /**
     * Recherche full-text dans le nom et la description.
     */
    @Query("SELECT n FROM KnowledgeNode n WHERE LOWER(n.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(n.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<KnowledgeNode> search(@Param("query") String query);
}
