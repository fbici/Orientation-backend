package com.orientation.orientationapp.modules.auth.service;

import com.orientation.orientationapp.modules.auth.repository.*;
import com.orientation.orientationapp.security.service.ResourceAccess;
import com.orientation.orientationapp.exception.BusinessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.Instant;

@Service @RequiredArgsConstructor
public class CandidateDeletionService {
    private final ResourceAccess access;
    private final UserRepository users;
    private final ExternalIdentityRepository identities;
    private final GoogleIdentityVerifier google;
    private final PasswordEncoder passwords;
    private final RefreshTokenService sessions;
    private final JdbcTemplate jdbc;
    public record Receipt(Instant purgeAfter,String message) {}

    @Transactional
    public Receipt request(String confirmation,String password,String googleIdToken) {
        var candidate=access.ownCandidate();
        var user=users.lockById(access.current().getId()).orElseThrow(ResourceAccess::denied);
        if(!"SUPPRIMER".equals(confirmation) || user.getUserRoles().isEmpty()
            || user.getUserRoles().stream().anyMatch(r -> !"CANDIDAT".equals(r.getRole().getCode())))
            throw ResourceAccess.denied();
        boolean verified=false;
        if(googleIdToken!=null && !googleIdToken.isBlank()) {
            var identity=google.verify(googleIdToken);
            verified=identities.findByIssuerAndSubject("https://accounts.google.com",identity.subject())
                .filter(i->i.getUser().getId().equals(user.getId())).isPresent();
        } else if(password!=null && !password.isBlank() && password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length<=72) {
            verified=passwords.matches(password,user.getPassword());
        }
        if(!verified)throw new BusinessException("Confirmez votre identité avec votre mot de passe ou le compte Google associé.","DELETION_IDENTITY_REQUIRED");
        // User row lock serializes this request with session issuance/profile changes.
        jdbc.update("INSERT INTO candidate_deletion_requests(user_id) VALUES (?) ON CONFLICT(user_id) DO NOTHING",user.getId());
        var due=jdbc.queryForObject("SELECT purge_after FROM candidate_deletion_requests WHERE user_id=?",java.sql.Timestamp.class,user.getId()).toInstant();
        user.setEnabled(false);users.saveAndFlush(user);sessions.revokeAllUserTokens(user.getId());
        jdbc.update("UPDATE api_keys SET active=false WHERE user_id=?",user.getId());
        jdbc.update("UPDATE account_tokens SET consumed=true WHERE user_id=?",user.getId());
        jdbc.update("UPDATE email_verification_challenges SET consumed=true WHERE user_id=?",user.getId());
        return new Receipt(due,"Demande enregistrée. Votre accès est désactivé. L’effacement interviendra après 30 jours ; votre adresse sera réutilisable une fois cet effacement terminé.");
    }
}
