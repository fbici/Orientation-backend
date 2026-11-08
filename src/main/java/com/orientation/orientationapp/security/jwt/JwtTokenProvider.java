package com.orientation.orientationapp.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {
    private static final String ISSUER = "orientia";
    private static final String AUDIENCE = "orientia-api";
    private final SecretKey key;
    private final long jwtExpiration;
    private final long refreshExpiration;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration:900000}") long expiration,
            @Value("${app.jwt.refresh-expiration:604800000}") long refreshExpiration) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32)
            throw new IllegalStateException("JWT_SECRET must contain at least 32 bytes");
        if (expiration < 1000 || expiration > 900000 || refreshExpiration <= expiration)
            throw new IllegalStateException("Invalid JWT lifetime configuration");
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpiration = expiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String generateAccessToken(String subject, String email) {
        return generateAccessToken(subject, email, UUID.randomUUID());
    }

    public String generateAccessToken(String subject, String email, UUID sessionId) {
        Date now = new Date();
        return Jwts.builder().issuer(ISSUER).audience().add(AUDIENCE).and()
                .subject(subject).claim("type", "access").claim("sid", sessionId.toString())
                .id(UUID.randomUUID().toString()).issuedAt(now)
                .expiration(new Date(now.getTime() + jwtExpiration)).signWith(key).compact();
    }

    /** Refresh credentials are opaque and are never JWTs. */
    public String generateRefreshToken(String subject, String email) {
        byte[] bytes = new byte[32];
        new java.security.SecureRandom().nextBytes(bytes);
        return "rt1_" + java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public Claims getAccessClaims(String token) {
        Claims claims = Jwts.parser().verifyWith(key).requireIssuer(ISSUER)
                .requireAudience(AUDIENCE).require("type", "access").build()
                .parseSignedClaims(token).getPayload();
        UUID.fromString(claims.getSubject());
        UUID.fromString(claims.get("sid", String.class));
        if (claims.getId() == null || claims.getIssuedAt() == null || claims.getExpiration() == null)
            throw new IllegalArgumentException("Invalid access claims");
        return claims;
    }

    public boolean validateToken(String token) {
        try { getAccessClaims(token); return true; }
        catch (RuntimeException ex) { return false; }
    }
    public String getSubjectFromToken(String token) { return getAccessClaims(token).getSubject(); }
    public String getEmailFromToken(String token) { return getAccessClaims(token).get("email", String.class); }
    public long getJwtExpiration() { return jwtExpiration; }
    public long getRefreshExpiration() { return refreshExpiration; }
}
