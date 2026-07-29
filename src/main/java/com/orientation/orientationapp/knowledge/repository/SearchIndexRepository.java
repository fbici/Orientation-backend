package com.orientation.orientationapp.knowledge.repository;

import com.orientation.orientationapp.knowledge.entity.SearchIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SearchIndexRepository extends JpaRepository<SearchIndex, UUID> {
    List<SearchIndex> findByEntityType(String entityType);
    List<SearchIndex> findByNameContainingIgnoreCase(String name);

    @Query("SELECT si FROM SearchIndex si WHERE si.active = true AND (LOWER(si.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(si.content) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<SearchIndex> search(@Param("query") String query);
}
