package com.orientation.orientationapp.modules.auth.service;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.json.gson.GsonFactory;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.web.server.ResponseStatusException;
import java.security.*;
import java.util.*;
class GoogleIdentityVerifierTest {
    private static final String CLIENT="test-web.apps.googleusercontent.com";
    private static final long NOW=1800000000L;
    private KeyPair keys;
    private GoogleIdentityVerifier service;
    private GoogleIdTokenVerifier verifier;
    @BeforeEach void setup() throws Exception {
        var generator=KeyPairGenerator.getInstance("RSA");generator.initialize(2048);keys=generator.generateKeyPair();
        var manager=mock(GooglePublicKeysManager.class);
        when(manager.getJsonFactory()).thenReturn(GsonFactory.getDefaultInstance());
        when(manager.getPublicKeys()).thenReturn(List.of(keys.getPublic()));
        verifier=new GoogleIdTokenVerifier.Builder(manager).setAudience(List.of(CLIENT)).setClock(()->NOW*1000).setAcceptableTimeSkewSeconds(0).build();
        service=new GoogleIdentityVerifier(true,CLIENT,verifier);
    }
    private String token(String audience,String issuer,long expiry,boolean verified,PrivateKey privateKey) {
        return Jwts.builder().issuer(issuer).subject("stable-google-subject").audience().add(audience).and()
            .issuedAt(new Date((NOW-60)*1000)).expiration(new Date(expiry*1000))
            .claim("email","Student@example.test").claim("email_verified",verified).claim("given_name","Student").claim("family_name","Test")
            .signWith(privateKey,Jwts.SIG.RS256).compact();
    }
    @Test void acceptsOnlyCryptographicallyVerifiedIdentity() {
        var identity=service.verify(token(CLIENT,"https://accounts.google.com",NOW+3600,true,keys.getPrivate()));
        assertEquals("stable-google-subject",identity.subject());assertEquals("student@example.test",identity.email());
    }
    @Test void rejectsWrongAudienceIssuerExpiryAndUnverifiedMail() {
        for(String raw:List.of(token("foreign.apps.googleusercontent.com","https://accounts.google.com",NOW+3600,true,keys.getPrivate()),
                token(CLIENT,"https://attacker.example",NOW+3600,true,keys.getPrivate()),
                token(CLIENT,"https://accounts.google.com",NOW-60,true,keys.getPrivate()),
                token(CLIENT,"https://accounts.google.com",NOW+3600,false,keys.getPrivate()))) {
            var error=assertThrows(ResponseStatusException.class,()->service.verify(raw));assertEquals(401,error.getStatusCode().value());
        }
    }
    @Test void rejectsForeignSignatureAndMalformedTokens() throws Exception {
        var generator=KeyPairGenerator.getInstance("RSA");generator.initialize(2048);
        String wrong=token(CLIENT,"https://accounts.google.com",NOW+3600,true,generator.generateKeyPair().getPrivate());
        assertEquals(401,assertThrows(ResponseStatusException.class,()->service.verify(wrong)).getStatusCode().value());
        assertEquals(401,assertThrows(ResponseStatusException.class,()->service.verify("not.a.token")).getStatusCode().value());
    }
    @Test void rejectsMissingExpirationClaim() {
        String raw=Jwts.builder().issuer("https://accounts.google.com").subject("stable-google-subject")
            .audience().add(CLIENT).and().issuedAt(new Date((NOW-60)*1000))
            .claim("email","student@example.test").claim("email_verified",true)
            .signWith(keys.getPrivate(),Jwts.SIG.RS256).compact();
        var error=assertThrows(ResponseStatusException.class,()->service.verify(raw));
        assertEquals(401,error.getStatusCode().value());
        assertEquals("INVALID_GOOGLE_TOKEN",error.getReason());
    }
    @Test void rejectsMissingIssuedAtClaim() {
        String raw=Jwts.builder().issuer("https://accounts.google.com").subject("stable-google-subject")
            .audience().add(CLIENT).and().expiration(new Date((NOW+3600)*1000))
            .claim("email","student@example.test").claim("email_verified",true)
            .signWith(keys.getPrivate(),Jwts.SIG.RS256).compact();
        var error=assertThrows(ResponseStatusException.class,()->service.verify(raw));
        assertEquals(401,error.getStatusCode().value());
        assertEquals("INVALID_GOOGLE_TOKEN",error.getReason());
    }
    @Test void disabledConfigurationDoesNotContactGoogle() {
        assertEquals(503,assertThrows(ResponseStatusException.class,()->new GoogleIdentityVerifier(false,CLIENT,verifier).verify("ignored")).getStatusCode().value());
        assertEquals(503,assertThrows(ResponseStatusException.class,()->new GoogleIdentityVerifier(true,"",verifier).verify("ignored")).getStatusCode().value());
    }
}
