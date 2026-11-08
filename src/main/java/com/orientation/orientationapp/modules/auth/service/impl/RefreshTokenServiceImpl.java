package com.orientation.orientationapp.modules.auth.service.impl;

import com.orientation.orientationapp.modules.auth.entity.*;
import com.orientation.orientationapp.modules.auth.repository.*;
import com.orientation.orientationapp.modules.auth.service.RefreshTokenService;
import com.orientation.orientationapp.security.jwt.JwtTokenProvider;
import com.orientation.orientationapp.security.service.AccountState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository users;
    private final JwtTokenProvider jwtTokenProvider;

    public static String digest(String token) {
        try { return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
    private void format(String token) {
        if (token == null || !token.matches("rt1_[A-Za-z0-9_-]{43}")) throw new InvalidRefreshException();
    }
    @Override @Transactional
    public RefreshToken createRefreshToken(User user, String ip, String agent) {
        return create(user, UUID.randomUUID(), Instant.now().plusMillis(jwtTokenProvider.getRefreshExpiration()), ip, agent);
    }
    private RefreshToken create(User user, UUID family, Instant expires, String ip, String agent) {
        String raw = jwtTokenProvider.generateRefreshToken(user.getId().toString(), null);
        RefreshToken entity = RefreshToken.builder().token(digest(raw)).user(user).familyId(family)
                .expiresAt(expires).ipAddress(ip).userAgent(agent).revoked(false).build();
        entity.setPlainToken(raw);
        return refreshTokenRepository.saveAndFlush(entity);
    }
    @Override @Transactional(readOnly=true)
    public RefreshToken validateRefreshToken(String token) {
        format(token);
        return refreshTokenRepository.findByToken(digest(token))
                .filter(t -> !Boolean.TRUE.equals(t.getRevoked()) && t.getExpiresAt().isAfter(Instant.now()))
                .filter(t -> AccountState.active(t.getUser())).orElseThrow(InvalidRefreshException::new);
    }
    @Override @Transactional(noRollbackFor=InvalidRefreshException.class)
    public RefreshToken rotateRefreshToken(String token, String ip, String agent) {
        format(token);
        UUID owner = refreshTokenRepository.findUserIdByToken(digest(token)).orElseThrow(InvalidRefreshException::new);
        // Load the token only after locking the account, avoiding stale first-level cache state.
        User user = users.lockById(owner).orElseThrow(InvalidRefreshException::new);
        RefreshToken old = refreshTokenRepository.lockByToken(digest(token)).orElseThrow(InvalidRefreshException::new);
        if (Boolean.TRUE.equals(old.getRevoked())) {
            refreshTokenRepository.revokeFamily(old.getFamilyId());
            throw new InvalidRefreshException();
        }
        if (!old.getExpiresAt().isAfter(Instant.now()) || !AccountState.active(user)) {
            refreshTokenRepository.revokeFamily(old.getFamilyId());
            throw new InvalidRefreshException();
        }
        old.setRevoked(true);
        RefreshToken next = create(user, old.getFamilyId(), old.getExpiresAt(), ip, agent);
        old.setReplacedBy(next);
        refreshTokenRepository.saveAndFlush(old);
        return next;
    }
    @Override @Transactional
    public void revokeRefreshToken(String token) {
        format(token);
        RefreshToken observed = refreshTokenRepository.findByToken(digest(token)).orElseThrow(InvalidRefreshException::new);
        users.lockById(observed.getUser().getId()).orElseThrow(InvalidRefreshException::new);
        refreshTokenRepository.revokeFamily(observed.getFamilyId());
    }
    @Override @Transactional
    public void revokeAllUserTokens(UUID userId) { refreshTokenRepository.revokeUser(userId); }
    @Override @Transactional
    public void cleanupExpiredTokens() { refreshTokenRepository.deleteByExpiresAtBefore(Instant.now()); }
    @Transactional(readOnly=true)
    public boolean isSessionActive(UUID family, UUID userId) {
        return refreshTokenRepository.existsByFamilyIdAndUserIdAndRevokedFalseAndExpiresAtAfter(family,userId,Instant.now());
    }
}
