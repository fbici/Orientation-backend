package com.orientation.orientationapp.modules.auth.repository;

import com.orientation.orientationapp.modules.auth.entity.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID>, JpaSpecificationExecutor<Tenant> {
    Optional<Tenant> findByCode(String code);
    List<Tenant> findByOrganizationId(UUID organizationId);
    boolean existsByCode(String code);

    @Query("SELECT t FROM Tenant t WHERE t.deleted = false AND t.organization.id = :orgId AND (:search IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Tenant> searchByOrganization(@Param("orgId") UUID orgId, @Param("search") String search, Pageable pageable);
}
