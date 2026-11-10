package com.orientation.orientationapp.modules.auth.repository;
import com.orientation.orientationapp.modules.auth.entity.ExternalIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ExternalIdentityRepository extends JpaRepository<ExternalIdentity,UUID> {
    Optional<ExternalIdentity> findByIssuerAndSubject(String issuer,String subject);
}
