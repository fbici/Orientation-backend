package com.orientation.orientationapp.modules.auth.service;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
@Slf4j
@Service
public class EmailService {
    private static final int MAX_MESSAGE_ID_LENGTH=1024;
    private static final int MAX_ACKNOWLEDGEMENT_BYTES=16*1024;
    private final ObjectMapper json;
    private final HttpClient client;
    @Autowired
    public EmailService(ObjectMapper json) {
        this(json, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
    }
    // Test seam: every email test supplies a mocked HTTP client.
    EmailService(ObjectMapper json, HttpClient client) {
        this.json=json;
        this.client=client;
    }
    @Value("${app.brevo.api-key:}") private String apiKey;
    @Value("${app.brevo.enabled:false}") private boolean enabled;
    @Value("${app.brevo.from-email:}") private String fromEmail;
    @Value("${app.brevo.sender-name:Orientia}") private String senderName;
    @Value("${app.base-url:}") private String baseUrl;
    @Value("${app.password-reset-url:}") private String resetUrl;
    public void requireConfigured() {
        if(!enabled || apiKey==null || apiKey.isBlank() || !safeApiKeyHeader(apiKey) || fromEmail==null
                || !fromEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || !validUrl(baseUrl) || (resetUrl!=null && !resetUrl.isBlank() && !validUrl(resetUrl))) {
            log.warn("Transactional email unavailable: configuration incomplete or disabled");
            throw unavailable();
        }
    }
    private static boolean safeApiKeyHeader(String value) {
        // Reject unsafe transport characters before HttpRequest can include a value in an exception.
        return value.chars().allMatch(character -> character>=0x20 && character<=0x7e);
    }
    private static boolean validUrl(String value) {
        try {
            URI uri=URI.create(value);
            return Set.of("https","http").contains(uri.getScheme()) && uri.getHost()!=null
                && uri.getUserInfo()==null && uri.getQuery()==null && uri.getFragment()==null;
        } catch(IllegalArgumentException | NullPointerException ex) { return false; }
    }
    public void sendVerificationEmail(String email,String name,String token) {
        requireConfigured();
        send(email,"Vérifiez votre adresse e-mail",name,
            "Confirmez votre adresse e-mail pour activer votre compte et préparer votre parcours universitaire.",
            "Vérifier mon adresse",baseUrl+"/auth/verify-email?token="+encodeToken(token),
            "Ce lien est valable 24 heures et ne peut être utilisé qu’une fois. Si vous n’avez pas créé ce compte, ignorez cet e-mail.");
    }
    public void sendPasswordResetEmail(String email,String name,String token) {
        requireConfigured();
        send(email,"Un nouveau départ, en toute sécurité",name,
            "Vous avez demandé à réinitialiser votre mot de passe Orientia. Choisissez un nouveau mot de passe pour retrouver votre espace.",
            "Réinitialiser mon mot de passe",(resetUrl==null || resetUrl.isBlank()?baseUrl+"/auth/reset-password":resetUrl)+"?token="+encodeToken(token),
            "Ce lien est valable une heure et ne peut être utilisé qu’une fois. Si cette demande ne vient pas de vous, ignorez cet e-mail : votre mot de passe reste inchangé.");
    }
    public void sendWelcomeEmail(String email,String name) {
        send(email,"Bienvenue sur Orientia",name,"Votre espace vous accompagne dans vos choix de formations, d’universités et de bourses.",
            "Découvrir Orientia",baseUrl,"Explorez les opportunités et leurs conditions avant de préparer votre candidature.");
    }
    public void sendAdministrativeNotification(String recipient,String message) {
        send(recipient,"Orientia — notification administrative",null,message,
            "Ouvrir le backoffice",baseUrl,"Consultez les informations détaillées uniquement dans votre espace administrateur sécurisé.");
    }
    private static String encodeToken(String token) {
        return java.net.URLEncoder.encode(token,StandardCharsets.UTF_8);
    }
    private void send(String to,String subject,String name,String message,String action,String url,String notice) {
        requireConfigured();
        try {
            Map<String,String> sender=new LinkedHashMap<>();
            sender.put("email",fromEmail);
            if(senderName!=null && !senderName.isBlank()) sender.put("name",senderName.strip());
            String body=json.writeValueAsString(Map.of("sender",sender,"to",List.of(Map.of("email",to)),
                "subject",subject,"htmlContent",EmailTemplateRenderer.render(baseUrl,subject,name,message,action,url,notice)));
            var request=HttpRequest.newBuilder(URI.create("https://api.brevo.com/v3/smtp/email"))
                .timeout(Duration.ofSeconds(15)).header("api-key",apiKey)
                .header("Accept","application/json").header("Content-Type","application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
            var response=client.send(request,info -> new BoundedAcknowledgementSubscriber());
            if(response.statusCode()!=201) {
                log.warn("Brevo rejected transactional email: HTTP {}",response.statusCode());
                throw unavailable();
            }
            if(!acceptedAcknowledgement(response.body())) {
                log.warn("Brevo transactional email acknowledgement invalid; delivery is unconfirmed");
                throw unavailable();
            }
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Brevo transactional email interrupted; delivery is unconfirmed");
            throw unavailable();
        } catch(java.io.IOException ex) {
            log.warn("Brevo transactional email transport failed; delivery is unconfirmed");
            throw unavailable();
        }
    }
    private static final class BoundedAcknowledgementSubscriber implements HttpResponse.BodySubscriber<String> {
        private final HttpResponse.BodySubscriber<String> delegate=HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
        private Flow.Subscription subscription;
        private long received;
        private boolean finished;
        @Override public CompletionStage<String> getBody() { return delegate.getBody(); }
        @Override public void onSubscribe(Flow.Subscription upstream) {
            subscription=upstream;
            delegate.onSubscribe(upstream);
        }
        @Override public void onNext(List<ByteBuffer> buffers) {
            if(finished) return;
            long bytes=buffers.stream().mapToLong(ByteBuffer::remaining).sum();
            if(bytes>MAX_ACKNOWLEDGEMENT_BYTES-received) {
                finished=true;
                subscription.cancel();
                delegate.onError(new java.io.IOException("Transactional email acknowledgement exceeds the response limit"));
                return;
            }
            received+=bytes;
            delegate.onNext(buffers);
        }
        @Override public void onError(Throwable error) {
            if(finished) return;
            finished=true;
            delegate.onError(error);
        }
        @Override public void onComplete() {
            if(finished) return;
            finished=true;
            delegate.onComplete();
        }
    }
    private boolean acceptedAcknowledgement(String body) {
        if(body==null || body.isBlank()) return false;
        try {
            JsonNode acknowledgement=json.reader().with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).readTree(body);
            if(acknowledgement==null || !acknowledgement.isObject()) return false;
            JsonNode messageId=acknowledgement.get("messageId");
            return messageId!=null && messageId.isTextual() && !messageId.textValue().isBlank()
                && messageId.textValue().length()<=MAX_MESSAGE_ID_LENGTH;
        } catch(java.io.IOException ex) { return false; }
    }
    private static ResponseStatusException unavailable() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Email delivery unavailable; no delivery is confirmed");
    }
}
