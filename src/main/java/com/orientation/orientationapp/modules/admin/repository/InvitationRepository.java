package com.orientation.orientationapp.modules.admin.repository;

import com.orientation.orientationapp.modules.admin.entity.Invitation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, UUID>, JpaSpecificationExecutor<Invitation> {
    Optional<Invitation> findByToken(String token);
    Page<Invitation> findByTenantId(UUID tenantId, Pageable pageable);
    boolean existsByEmailAndTenantIdAndStatus(String email, UUID tenantId, Invitation.InvitationStatus status);
}
