package com.orientation.orientationapp.security;
import com.orientation.orientationapp.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
class JwtTokenProviderTest {
    private static final String KEY="unit-only-cryptographic-material-01234567890123456789";
    private final JwtTokenProvider provider=new JwtTokenProvider(KEY,900000,604800000);
    @Test void acceptsTypedAccessAndRejectsOpaqueRefresh() {
        String user=UUID.randomUUID().toString();
        assertTrue(provider.validateToken(provider.generateAccessToken(user,null)));
        assertFalse(provider.validateToken(provider.generateRefreshToken(user,null)));
    }
    @Test void rejectsLegacyRefreshAndMissingTypeAudience() {
        var key=Keys.hmacShaKeyFor(KEY.getBytes(StandardCharsets.UTF_8));
        String legacy=Jwts.builder().subject(UUID.randomUUID().toString())
            .claim("email","nobody@example.test").expiration(new Date(System.currentTimeMillis()+3600000)).signWith(key).compact();
        assertFalse(provider.validateToken(legacy));
        String wrong=Jwts.builder().issuer("orientia").audience().add("other-api").and()
            .claim("type","access").subject(UUID.randomUUID().toString()).signWith(key).compact();
        assertFalse(provider.validateToken(wrong));
    }
    @Test void rejectsExpiredAccess() {
        String expired=Jwts.builder().issuer("orientia").audience().add("orientia-api").and()
            .claim("type","access").claim("sid",UUID.randomUUID().toString())
            .subject(UUID.randomUUID().toString()).id(UUID.randomUUID().toString())
            .issuedAt(new Date(0)).expiration(new Date(1000))
            .signWith(Keys.hmacShaKeyFor(KEY.getBytes(StandardCharsets.UTF_8))).compact();
        assertFalse(provider.validateToken(expired));
    }
    @Test void accessAndRefreshAreUniqueAndCarryNoEmail() {
        String id=UUID.randomUUID().toString();String a=provider.generateAccessToken(id,null);
        assertNotEquals(a,provider.generateAccessToken(id,null));
        assertNull(provider.getAccessClaims(a).get("email"));
        assertNotEquals(provider.generateRefreshToken(id,null),provider.generateRefreshToken(id,null));
    }
    @Test void refusesMissingOrWeakSigningConfiguration() {
        assertThrows(IllegalStateException.class,()->new JwtTokenProvider("",900000,604800000));
        assertThrows(IllegalStateException.class,()->new JwtTokenProvider(KEY,86400000,604800000));
    }
}
