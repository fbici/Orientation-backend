package com.orientation.orientationapp.modules.auth.service.impl;

import com.orientation.orientationapp.modules.auth.entity.RefreshToken;
import com.orientation.orientationapp.modules.auth.entity.User;
import com.orientation.orientationapp.modules.auth.repository.RefreshTokenRepository;
import com.orientation.orientationapp.modules.auth.service.RefreshTokenService;
import com.orientation.orientationapp.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user, String ipAddress, String userAgent) {
        String token = jwtTokenProvider.generateRefreshToken(user.getId().toString(), user.getEmail());

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(Instant.now().plusMillis(refreshExpiration))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
        return refreshTokenRepository.findByTokenAndRevokedFalse(token)
                .filter(rt -> rt.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new RuntimeException("Invalid or expired refresh token"));
    }

    @Override
    @Transactional
    public RefreshToken rotateRefreshToken(String oldToken, String ipAddress, String userAgent) {
        RefreshToken oldRefreshToken = validateRefreshToken(oldToken);

        oldRefreshToken.setRevoked(true);

        User user = oldRefreshToken.getUser();
        RefreshToken newRefreshToken = createRefreshToken(user, ipAddress, userAgent);
        oldRefreshToken.setReplacedBy(newRefreshToken);

        refreshTokenRepository.save(oldRefreshToken);

        return newRefreshToken;
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Override
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }
}
