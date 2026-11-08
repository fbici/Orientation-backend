package com.orientation.orientationapp.modules.auth.repository;

import com.orientation.orientationapp.modules.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select t from RefreshToken t where t.token=:token")
    Optional<RefreshToken> lockByToken(@org.springframework.data.repository.query.Param("token") String token);
    @org.springframework.data.jpa.repository.Modifying(flushAutomatically=true)
    @org.springframework.data.jpa.repository.Query("update RefreshToken t set t.revoked=true where t.familyId=:family")
    void revokeFamily(@org.springframework.data.repository.query.Param("family") UUID family);
    @org.springframework.data.jpa.repository.Modifying(flushAutomatically=true)
    @org.springframework.data.jpa.repository.Query("update RefreshToken t set t.revoked=true where t.user.id=:userId")
    void revokeUser(@org.springframework.data.repository.query.Param("userId") UUID userId);
    @org.springframework.data.jpa.repository.Modifying(flushAutomatically=true)
    @org.springframework.data.jpa.repository.Query("update RefreshToken t set t.revoked=true where t.user.id in (select u.id from User u where u.tenant.id=:tenant)")
    void revokeTenant(@org.springframework.data.repository.query.Param("tenant") UUID tenant);
    boolean existsByFamilyIdAndUserIdAndRevokedFalseAndExpiresAtAfter(UUID familyId, UUID userId, Instant now);
    @org.springframework.data.jpa.repository.Query("select t.user.id from RefreshToken t where t.token=:token")
    Optional<UUID> findUserIdByToken(@org.springframework.data.repository.query.Param("token") String token);
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);
    void deleteByExpiresAtBefore(Instant expiresAt);
    void deleteByUserId(UUID userId);
}
