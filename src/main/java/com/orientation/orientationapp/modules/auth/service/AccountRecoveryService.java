package com.orientation.orientationapp.modules.auth.service;
import com.orientation.orientationapp.modules.auth.entity.*;
import com.orientation.orientationapp.modules.auth.repository.*;
import com.orientation.orientationapp.modules.auth.service.impl.RefreshTokenServiceImpl;
import com.orientation.orientationapp.modules.user.repository.CandidateRepository;
import com.orientation.orientationapp.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.Instant;
import java.security.SecureRandom;
import java.util.Base64;
@Service @RequiredArgsConstructor
public class AccountRecoveryService {
    private final AccountTokenRepository tokens;
    private final UserRepository users;
    private final CandidateRepository candidates;
    private final EmailService email;
    private final PasswordEncoder encoder;
    private final RefreshTokenService sessions;
    @Transactional
    public void requestReset(String address) {
        email.requireConfigured();
        users.findByEmail(address).filter(u -> Boolean.TRUE.equals(u.getEnabled())).ifPresent(user -> {
            String raw=issue(user,AccountToken.Purpose.RESET_PASSWORD,3600);
            email.sendPasswordResetEmail(user.getEmail(),user.getFirstName(),raw);
        });
    }
    public String issue(User user,AccountToken.Purpose purpose,long seconds) {
        byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);
        String raw=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        AccountToken token=new AccountToken();token.setTokenHash(RefreshTokenServiceImpl.digest(raw));
        token.setUser(user);token.setPurpose(purpose);token.setExpiresAt(Instant.now().plusSeconds(seconds));
        tokens.saveAndFlush(token);return raw;
    }
    @Transactional
    public void verificationEmail(User user) {
        email.requireConfigured();
        String raw=issue(user,AccountToken.Purpose.VERIFY_EMAIL,86400);
        email.sendVerificationEmail(user.getEmail(),user.getFirstName(),raw);
    }
    private AccountToken consume(String raw,AccountToken.Purpose purpose) {
        if(raw==null || !raw.matches("[A-Za-z0-9_-]{43}")) throw invalid();
        String hash=RefreshTokenServiceImpl.digest(raw);
        java.util.UUID owner=tokens.findOwnerByHash(hash).orElseThrow(AccountRecoveryService::invalid);
        users.lockById(owner).orElseThrow(AccountRecoveryService::invalid);
        AccountToken token=tokens.lockByHash(hash).orElseThrow(AccountRecoveryService::invalid);
        if(token.isConsumed() || token.getPurpose()!=purpose || !token.getExpiresAt().isAfter(Instant.now())
            || !Boolean.TRUE.equals(token.getUser().getEnabled())) throw invalid();
        token.setConsumed(true);tokens.save(token);return token;
    }
    @Transactional
    public void reset(String raw,String password) {
        if(password==null || password.length()<12 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
            throw new BusinessException("Password must contain 12 to 72 bytes","INVALID_PASSWORD");
        User user=consume(raw,AccountToken.Purpose.RESET_PASSWORD).getUser();
        user.setPassword(encoder.encode(password));user.setPasswordChangedAt(Instant.now());
        user.setAccountLocked(false);user.setLockedUntil(null);user.setFailedLoginAttempts(0);
        users.saveAndFlush(user);tokens.consumeAll(user.getId());sessions.revokeAllUserTokens(user.getId());
    }
    @Transactional
    public void verify(String raw) {
        User user=consume(raw,AccountToken.Purpose.VERIFY_EMAIL).getUser();
        user.setEmailVerified(true);users.save(user);
        candidates.findByUserId(user.getId()).ifPresent(c -> { c.setVerified(true);candidates.save(c); });
    }
    private static BusinessException invalid() { return new BusinessException("Invalid or expired account token","INVALID_ACCOUNT_TOKEN"); }
}
