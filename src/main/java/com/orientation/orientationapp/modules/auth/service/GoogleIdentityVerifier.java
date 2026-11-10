package com.orientation.orientationapp.modules.auth.service;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
@Service
public class GoogleIdentityVerifier {
    public record Identity(String subject,String email,String firstName,String lastName) {}
    private final boolean enabled;
    private final String audience;
    private final GoogleIdTokenVerifier verifier;
    @Autowired
    public GoogleIdentityVerifier(@Value("${app.google.enabled:${GOOGLE_ENABLED:false}}") boolean enabled,
            @Value("${app.google.web-client-id:${GOOGLE_WEB_CLIENT_ID:}}") String audience) {
        this(enabled,audience,new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),GsonFactory.getDefaultInstance())
            .setAudience(List.of(audience)).setAcceptableTimeSkewSeconds(30).build());
    }
    GoogleIdentityVerifier(boolean enabled,String audience,GoogleIdTokenVerifier verifier) {
        this.enabled=enabled;this.audience=audience;this.verifier=verifier;
    }
    public Identity verify(String raw) {
        if(!enabled || !audience.matches("[A-Za-z0-9-]+[.]apps[.]googleusercontent[.]com"))
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"GOOGLE_SIGNIN_UNAVAILABLE");
        if(raw==null || raw.length()>16000 || raw.isBlank()) throw invalid();
        com.google.api.client.googleapis.auth.oauth2.GoogleIdToken token;
        try { token=com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.parse(verifier.getJsonFactory(),raw); }
        catch(java.io.IOException | IllegalArgumentException ex) { throw invalid(); }
        try {
            var p=token.getPayload();
            // The Google verifier unboxes these claims during temporal validation.
            if(p.getExpirationTimeSeconds()==null || p.getIssuedAtTimeSeconds()==null) throw invalid();
            if(!verifier.verify(token)) throw invalid();
            if(!Boolean.TRUE.equals(p.getEmailVerified()) || p.getSubject()==null || p.getSubject().isBlank()
                    || p.getSubject().length()>255 || p.getEmail()==null || p.getEmail().length()>255
                    || !p.getEmail().matches("[^\s@]+@[^\s@]+[.][^\s@]+")) throw invalid();
            return new Identity(p.getSubject(),p.getEmail().toLowerCase(Locale.ROOT),name(p.get("given_name"),"Membre"),name(p.get("family_name"),"Orientia"));
        } catch(java.io.IOException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"GOOGLE_SIGNIN_UNAVAILABLE");
        } catch(java.security.GeneralSecurityException | IllegalArgumentException ex) { throw invalid(); }
    }
    private String name(Object value,String fallback) {
        String s=value instanceof String text?text.strip():fallback;
        if(s.isEmpty())s=fallback;
        return s.substring(0,Math.min(s.length(),100));
    }
    private ResponseStatusException invalid(){return new ResponseStatusException(HttpStatus.UNAUTHORIZED,"INVALID_GOOGLE_TOKEN");}
}
