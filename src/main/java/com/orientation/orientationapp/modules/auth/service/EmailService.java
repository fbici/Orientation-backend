package com.orientation.orientationapp.modules.auth.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
@Service @RequiredArgsConstructor
public class EmailService {
    private final ObjectMapper json;
    @Value("${app.sendgrid.api-key:}") private String apiKey;
    @Value("${app.sendgrid.enabled:false}") private boolean enabled;
    @Value("${app.sendgrid.from-email:noreply@orientia.com}") private String fromEmail;
    @Value("${app.base-url:https://orientation-backend-4lcp.onrender.com}") private String baseUrl;
    @Value("${app.password-reset-url:}") private String resetUrl;
    public void requireConfigured() {
        if(!enabled || apiKey==null || apiKey.isBlank()) throw unavailable();
    }
    public void sendVerificationEmail(String email,String name,String token) {
        send(email,"Verify your email",baseUrl+"/auth/verify?token="+token);
    }
    public void sendPasswordResetEmail(String email,String name,String token) {
        send(email,"Reset your password",(resetUrl.isBlank()?baseUrl+"/auth/reset-password":resetUrl)+"?token="+token);
    }
    public void sendWelcomeEmail(String email,String name) { send(email,"Welcome",baseUrl); }
    private void send(String to,String subject,String url) {
        requireConfigured();
        try {
            String body=json.writeValueAsString(Map.of("personalizations",List.of(Map.of("to",List.of(Map.of("email",to)))),
                "from",Map.of("email",fromEmail),"subject",subject,
                "content",List.of(Map.of("type","text/plain","value",subject+": "+url))));
            var request=HttpRequest.newBuilder(URI.create("https://api.sendgrid.com/v3/mail/send"))
                .timeout(Duration.ofSeconds(15)).header("Authorization","Bearer "+apiKey)
                .header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
            var client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
            int status=client.send(request,HttpResponse.BodyHandlers.discarding()).statusCode();
            if(status<200 || status>=300) throw unavailable();
        } catch(InterruptedException ex) { Thread.currentThread().interrupt();throw unavailable(); }
        catch(java.io.IOException ex) { throw unavailable(); }
    }
    private static ResponseStatusException unavailable() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Email delivery unavailable; no delivery is confirmed");
    }
}
