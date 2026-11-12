package com.orientation.orientationapp.modules.auth.repository;

import com.orientation.orientationapp.modules.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<User> {
    @org.springframework.data.jpa.repository.Query(value="SELECT count(*) FROM candidate_deletion_requests d JOIN users u ON u.id=d.user_id WHERE lower(u.email)=lower(:email)",nativeQuery=true)
    long countDeletionByEmail(@org.springframework.data.repository.query.Param("email") String email);
    @org.springframework.data.jpa.repository.Query(value = "SELECT count(*) FROM users u JOIN user_roles ur ON ur.user_id=u.id JOIN roles r ON r.id=ur.role_id WHERE r.code='SUPER_ADMIN'", nativeQuery=true)
    long countAllSuperAdministrators();
    @org.springframework.data.jpa.repository.Query(value="SELECT count(*) FROM users WHERE lower(email)=lower(:email)", nativeQuery=true)
    long countAllByEmail(@org.springframework.data.repository.query.Param("email") String email);
    @org.springframework.data.jpa.repository.Query(value="SELECT count(*) FROM users WHERE lower(email)=lower(:email) AND (enabled=false OR deleted=true)", nativeQuery=true)
    long countInactiveByEmail(@org.springframework.data.repository.query.Param("email") String email);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from User u where u.id=:id")
    Optional<User> lockById(@org.springframework.data.repository.query.Param("id") UUID id);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from User u where u.email=:email")
    Optional<User> lockByEmail(@org.springframework.data.repository.query.Param("email") String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndTenant_Id(String email, UUID tenantId);
    boolean existsByEmail(String email);
    List<User> findByTenant_Id(UUID tenantId);
    List<User> findByStatus(com.orientation.orientationapp.common.enums.CandidateStatus status);
}
