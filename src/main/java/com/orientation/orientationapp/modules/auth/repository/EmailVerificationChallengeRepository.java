package com.orientation.orientationapp.modules.auth.repository;
import com.orientation.orientationapp.modules.auth.entity.EmailVerificationChallenge;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface EmailVerificationChallengeRepository extends JpaRepository<EmailVerificationChallenge,UUID> {
 Optional<EmailVerificationChallenge> findByUser_Id(UUID userId);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select c from EmailVerificationChallenge c where c.id=:id")
 Optional<EmailVerificationChallenge> lockById(@Param("id") UUID id);
}
