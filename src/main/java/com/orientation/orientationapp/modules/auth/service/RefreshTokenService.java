package com.orientation.orientationapp.modules.auth.service;

import com.orientation.orientationapp.modules.auth.entity.RefreshToken;
import com.orientation.orientationapp.modules.auth.entity.User;

import java.util.UUID;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user, String ipAddress, String userAgent);
    RefreshToken validateRefreshToken(String token);
    RefreshToken rotateRefreshToken(String oldToken, String ipAddress, String userAgent);
    void revokeRefreshToken(String token);
    void revokeAllUserTokens(UUID userId);
    void cleanupExpiredTokens();
}
