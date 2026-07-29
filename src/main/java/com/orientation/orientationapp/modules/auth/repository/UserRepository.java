package com.orientation.orientationapp.modules.auth.repository;

import com.orientation.orientationapp.modules.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndTenant_Id(String email, UUID tenantId);
    boolean existsByEmail(String email);
    List<User> findByTenant_Id(UUID tenantId);
    List<User> findByStatus(com.orientation.orientationapp.common.enums.CandidateStatus status);
}
