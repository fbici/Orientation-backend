package com.orientation.orientationapp.modules.auth.repository;
import com.orientation.orientationapp.modules.auth.entity.AccountToken;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface AccountTokenRepository extends JpaRepository<AccountToken,UUID> {
    @Modifying @Query("update AccountToken t set t.consumed=true where t.user.id=:userId")
    void consumeAll(@Param("userId") UUID userId);
    @Query("select t.user.id from AccountToken t where t.tokenHash=:hash")
    Optional<UUID> findOwnerByHash(@Param("hash") String hash);
    Optional<AccountToken> findByTokenHash(String hash);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select t from AccountToken t where t.tokenHash=:hash")
    Optional<AccountToken> lockByHash(@Param("hash") String hash);
}
