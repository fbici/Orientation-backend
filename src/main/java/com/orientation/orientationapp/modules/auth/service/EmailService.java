package com.orientation.orientationapp.modules.auth.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
@Slf4j
@Service
public class EmailService {
    private final ObjectMapper json;
    private final HttpClient client;
    @Autowired
    public EmailService(ObjectMapper json) {
        this(json, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
    }
    // Test seam: production endpoint and provider remain unchanged.
    EmailService(ObjectMapper json, HttpClient client) {
        this.json=json;
        this.client=client;
    }
    @Value("${app.sendgrid.api-key:}") private String apiKey;
    @Value("${app.sendgrid.enabled:false}") private boolean enabled;
    @Value("${app.sendgrid.from-email:}") private String fromEmail;
    @Value("${app.base-url:}") private String baseUrl;
    @Value("${app.password-reset-url:}") private String resetUrl;
    public void requireConfigured() {
        if(!enabled || apiKey==null || apiKey.isBlank() || fromEmail==null
                || !fromEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || !validUrl(baseUrl) || (resetUrl!=null && !resetUrl.isBlank() && !validUrl(resetUrl))) {
            log.warn("Transactional email unavailable: configuration incomplete or disabled");
            throw unavailable();
        }
    }
    private static boolean validUrl(String value) {
        try {
            URI uri=URI.create(value);
            return Set.of("https","http").contains(uri.getScheme()) && uri.getHost()!=null
                && uri.getUserInfo()==null && uri.getQuery()==null && uri.getFragment()==null;
        } catch(IllegalArgumentException | NullPointerException ex) { return false; }
    }
    public void sendVerificationEmail(String email,String name,String token) {
        send(email,"Verify your email",baseUrl+"/auth/verify-email?token="+token);
    }
    public void sendPasswordResetEmail(String email,String name,String token) {
        send(email,"Reset your password",(resetUrl.isBlank()?baseUrl+"/auth/reset-password":resetUrl)+"?token="+token);
    }
    public void sendWelcomeEmail(String email,String name) { send(email,"Welcome",baseUrl); }
    public void sendAdministrativeNotification(String recipient,String message) {
        send(recipient,"Orientia — notification administrative",message);
    }
    private void send(String to,String subject,String url) {
        requireConfigured();
        try {
            String body=json.writeValueAsString(Map.of("personalizations",List.of(Map.of("to",List.of(Map.of("email",to)))),
                "from",Map.of("email",fromEmail),"subject",subject,
                "content",List.of(Map.of("type","text/plain","value",subject+": "+url))));
            var request=HttpRequest.newBuilder(URI.create("https://api.sendgrid.com/v3/mail/send"))
                .timeout(Duration.ofSeconds(15)).header("Authorization","Bearer "+apiKey)
                .header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
            int status=client.send(request,HttpResponse.BodyHandlers.discarding()).statusCode();
            if(status<200 || status>=300) {
                log.warn("SendGrid rejected transactional email: HTTP {}",status);
                throw unavailable();
            }
        } catch(InterruptedException ex) { Thread.currentThread().interrupt();log.warn("SendGrid transactional email interrupted; delivery is unconfirmed");throw unavailable(); }
        catch(java.io.IOException ex) { log.warn("SendGrid transactional email transport failed; delivery is unconfirmed");throw unavailable(); }
    }
    private static ResponseStatusException unavailable() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Email delivery unavailable; no delivery is confirmed");
    }
}
