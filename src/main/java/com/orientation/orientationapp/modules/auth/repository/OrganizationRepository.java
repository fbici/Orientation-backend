package com.orientation.orientationapp.modules.auth.repository;

import com.orientation.orientationapp.modules.auth.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID>, JpaSpecificationExecutor<Organization> {
    Optional<Organization> findByName(String name);
    Optional<Organization> findByCode(String code);
    boolean existsByName(String name);
    boolean existsByCode(String code);

    @Query("SELECT o FROM Organization o WHERE o.deleted = false AND (:search IS NULL OR LOWER(o.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(o.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Organization> search(@Param("search") String search, Pageable pageable);
}
