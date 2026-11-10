package com.orientation.orientationapp.modules.auth.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {
    private HttpClient client;
    private EmailService service;
    private ListAppender<ILoggingEvent> logs;
    private Logger logger;
    @BeforeEach void setup() {
        client=mock(HttpClient.class);
        service=new EmailService(new ObjectMapper(),client);
        ReflectionTestUtils.setField(service,"enabled",true);
        ReflectionTestUtils.setField(service,"apiKey","synthetic-mail-test-only");
        ReflectionTestUtils.setField(service,"fromEmail","sender@example.invalid");
        ReflectionTestUtils.setField(service,"senderName","Orientia");
        ReflectionTestUtils.setField(service,"baseUrl","https://links.example.invalid");
        ReflectionTestUtils.setField(service,"resetUrl","");
        logger=(Logger)LoggerFactory.getLogger(EmailService.class);
        logs=new ListAppender<>();logs.start();logger.addAppender(logs);
    }
    @AfterEach void cleanup() {
        logger.detachAppender(logs);logs.stop();Thread.interrupted();
        String text=logs.list.stream().map(ILoggingEvent::getFormattedMessage).reduce("",String::concat);
        for(String privateValue:new String[]{"synthetic-mail-test-only","private-account-token","recipient@example.invalid",
                "sender@example.invalid","provider-private-detail","queued-private-provider-id","private-header-value"})
            assertFalse(text.contains(privateValue));
    }
    @SuppressWarnings("unchecked")
    private void respond(int status,String body) throws Exception {
        HttpResponse<String> response=mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);when(response.body()).thenReturn(body);
        when(client.send(any(HttpRequest.class),any(HttpResponse.BodyHandler.class))).thenReturn(response);
    }
    private void accepted() throws Exception {respond(201,"{\"messageId\":\"queued-private-provider-id\"}");}
    @Test void acceptedRequestUsesBrevoApiAndPreservesTheVerificationLink() throws Exception {
        accepted();
        service.sendVerificationEmail("recipient@example.invalid","Synthetic","private-account-token");
        var request=ArgumentCaptor.forClass(HttpRequest.class);
        verify(client).send(request.capture(),any(HttpResponse.BodyHandler.class));
        assertEquals("https://api.brevo.com/v3/smtp/email",request.getValue().uri().toString());
        assertEquals("POST",request.getValue().method());
        assertEquals("synthetic-mail-test-only",request.getValue().headers().firstValue("api-key").orElseThrow());
        assertTrue(request.getValue().headers().firstValue("Authorization").isEmpty());
        assertEquals("application/json",request.getValue().headers().firstValue("Accept").orElseThrow());
        assertEquals(Duration.ofSeconds(15),request.getValue().timeout().orElseThrow());
        var json=new ObjectMapper().readTree(body(request.getValue()));
        assertEquals("sender@example.invalid",json.path("sender").path("email").asText());
        assertEquals("Orientia",json.path("sender").path("name").asText());
        assertEquals("recipient@example.invalid",json.path("to").get(0).path("email").asText());
        assertEquals("Vérifiez votre adresse e-mail",json.path("subject").asText());
        assertTrue(json.path("htmlContent").asText().contains("https://links.example.invalid/auth/verify-email?token=private-account-token"));
        assertFalse(json.has("personalizations"));assertFalse(json.has("textContent"));
    }
    @Test void administrativeEmailUsesBrevoAndTheConfiguredSender() throws Exception {
        accepted();
        service.sendAdministrativeNotification("adminorientia@gmail.com","Une alerte est disponible : https://links.example.invalid/notifications");
        var request=ArgumentCaptor.forClass(HttpRequest.class);
        verify(client).send(request.capture(),any(HttpResponse.BodyHandler.class));
        var json=new ObjectMapper().readTree(body(request.getValue()));
        assertEquals("adminorientia@gmail.com",json.path("to").get(0).path("email").asText());
        assertEquals("sender@example.invalid",json.path("sender").path("email").asText());
        assertTrue(json.path("htmlContent").asText().contains("/notifications"));
    }
    @Test void resetUsesConfiguredPage() throws Exception {
        accepted();ReflectionTestUtils.setField(service,"resetUrl","https://links.example.invalid/auth/reset-password");
        service.sendPasswordResetEmail("recipient@example.invalid","Synthetic","private-account-token");
        var request=ArgumentCaptor.forClass(HttpRequest.class);verify(client).send(request.capture(),any(HttpResponse.BodyHandler.class));
        assertTrue(body(request.getValue()).contains("https://links.example.invalid/auth/reset-password?token=private-account-token"));
    }
    @Test void resetUsesTheExistingFallbackPage() throws Exception {
        accepted();service.sendPasswordResetEmail("recipient@example.invalid","Synthetic","private-account-token");
        var request=ArgumentCaptor.forClass(HttpRequest.class);verify(client).send(request.capture(),any(HttpResponse.BodyHandler.class));
        assertTrue(body(request.getValue()).contains("https://links.example.invalid/auth/reset-password?token=private-account-token"));
    }
    @Test void welcomeEmailKeepsItsPublicMethodAndContent() throws Exception {
        accepted();service.sendWelcomeEmail("recipient@example.invalid","Synthetic");
        var request=ArgumentCaptor.forClass(HttpRequest.class);verify(client).send(request.capture(),any(HttpResponse.BodyHandler.class));
        var json=new ObjectMapper().readTree(body(request.getValue()));
        assertEquals("Bienvenue sur Orientia",json.path("subject").asText());
        assertTrue(json.path("htmlContent").asText().contains("Découvrir Orientia"));
    }
    @Test void optionalSenderNameCanBeOmitted() throws Exception {
        accepted();ReflectionTestUtils.setField(service,"senderName","   ");
        service.sendVerificationEmail("recipient@example.invalid","Synthetic","private-account-token");
        var request=ArgumentCaptor.forClass(HttpRequest.class);verify(client).send(request.capture(),any(HttpResponse.BodyHandler.class));
        var json=new ObjectMapper().readTree(body(request.getValue()));
        assertFalse(json.path("sender").has("name"));
    }
    @ParameterizedTest @ValueSource(ints={200,202,204,400,401,403,422,429,500})
    void everyNon201ResponseIsRejectedWithoutPrivateDetails(int status) throws Exception {
        respond(status,"{\"message\":\"provider-private-detail\"}");
        var failure=assertThrows(ResponseStatusException.class,()->service.sendVerificationEmail("recipient@example.invalid","Synthetic","private-account-token"));
        assertEquals(503,failure.getStatusCode().value());
        assertTrue(logs.list.stream().anyMatch(e->e.getFormattedMessage().contains("HTTP "+status)));
        assertEquals("Email delivery unavailable; no delivery is confirmed",failure.getReason());
    }
    @ParameterizedTest @NullAndEmptySource
    @ValueSource(strings={"not-json","null","[]","{}","{\"messageId\":null}","{\"messageId\":42}","{\"messageId\":true}",
        "{\"messageId\":\"\"}","{\"messageId\":\"   \"}","{\"messageIds\":[\"queued-private-provider-id\"]}",
        "{\"messageId\":\"queued-private-provider-id\"} {\"unexpected\":true}"})
    void rejectsMissingInvalidOrIncomplete201Acknowledgements(String acknowledgement) throws Exception {
        respond(201,acknowledgement);
        var failure=assertThrows(ResponseStatusException.class,()->service.sendVerificationEmail("recipient@example.invalid","Synthetic","private-account-token"));
        assertEquals(503,failure.getStatusCode().value());
        assertEquals("Email delivery unavailable; no delivery is confirmed",failure.getReason());
        assertTrue(logs.list.stream().anyMatch(e->e.getFormattedMessage().contains("acknowledgement invalid")));
    }
    @Test void rejectsAnOversizedMessageIdentifier() throws Exception {
        respond(201,new ObjectMapper().writeValueAsString(Map.of("messageId","x".repeat(1025))));
        assertEquals(503,assertThrows(ResponseStatusException.class,()->service.sendVerificationEmail(
            "recipient@example.invalid","Synthetic","private-account-token")).getStatusCode().value());
    }
    @ParameterizedTest @ValueSource(strings={"<201798300811.5787683@relay.domain.com>","provider-identifier-without-angle-brackets"})
    void acceptsDocumentedAndOtherNonEmptyStringIdentifiers(String messageId) throws Exception {
        respond(201,new ObjectMapper().writeValueAsString(Map.of("messageId",messageId)));
        assertDoesNotThrow(()->service.sendVerificationEmail("recipient@example.invalid","Synthetic","private-account-token"));
    }
    @Test void productionYamlBindsTheBrevoEnvironmentVariables() {
        productionContext(Map.of("BREVO_ENABLED","true","BREVO_API_KEY","synthetic-mail-test-only",
            "BREVO_FROM_EMAIL","sender@example.invalid","BREVO_SENDER_NAME","Orientia"))
            .run(context -> {
                assertNull(context.getStartupFailure());
                context.getBean(EmailService.class).requireConfigured();
                assertEquals("Orientia",ReflectionTestUtils.getField(context.getBean(EmailService.class),"senderName"));
                verifyNoInteractions(client);
            });
    }
    @Test void productionDefaultsEnableBrevoButNeverSendWithoutAnApiKeyAndSender() {
        productionContext(Map.of("BREVO_API_KEY","","BREVO_FROM_EMAIL",""))
            .run(context -> {
                assertNull(context.getStartupFailure());
                var email=context.getBean(EmailService.class);
                assertEquals(true,ReflectionTestUtils.getField(email,"enabled"));
                assertEquals("Orientia",ReflectionTestUtils.getField(email,"senderName"));
                assertEquals(503,assertThrows(ResponseStatusException.class,email::requireConfigured).getStatusCode().value());
                verifyNoInteractions(client);
            });
    }
    private org.springframework.boot.test.context.runner.ApplicationContextRunner productionContext(Map<String,Object> properties) {
        return new org.springframework.boot.test.context.runner.ApplicationContextRunner()
            .withInitializer(context -> {
                var sources=context.getEnvironment().getPropertySources();
                sources.remove(org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
                sources.remove(org.springframework.core.env.StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
                var values=new java.util.HashMap<String,Object>(properties);values.put("APP_BASE_URL","https://links.example.invalid");
                sources.addFirst(new org.springframework.core.env.MapPropertySource("synthetic-env",values));
                try {
                    for(var source:new org.springframework.boot.env.YamlPropertySourceLoader().load("actual-prod",
                            new org.springframework.core.io.ClassPathResource("application-prod.yml"))) sources.addLast(source);
                } catch(IOException ex) { throw new IllegalStateException(ex); }
            })
            .withBean(EmailService.class, () -> new EmailService(new ObjectMapper(),client));
    }
    @Test void unsafeApiKeyCharactersAreRejectedBeforeRequestConstruction() {
        for(char unsafe:new char[]{0,'\r','\n','\t',127,256}) {
            String malformed="private-header-value"+unsafe+"suffix";
            ReflectionTestUtils.setField(service,"apiKey",malformed);
            var failure=assertThrows(ResponseStatusException.class,service::requireConfigured);
            assertEquals(503,failure.getStatusCode().value());
            assertFalse(failure.getReason().contains(malformed));
        }
        verifyNoInteractions(client);
    }
    @SuppressWarnings("unchecked")
    private HttpResponse.BodySubscriber<String> acknowledgementSubscriber() throws Exception {
        accepted();service.sendVerificationEmail("recipient@example.invalid","Synthetic","private-account-token");
        ArgumentCaptor<HttpResponse.BodyHandler<String>> handler=ArgumentCaptor.forClass(HttpResponse.BodyHandler.class);
        verify(client).send(any(HttpRequest.class),handler.capture());
        return handler.getValue().apply(mock(HttpResponse.ResponseInfo.class));
    }
    @Test void acknowledgementSubscriberCancelsOversizedResponsesBeforeBufferingThem() throws Exception {
        var subscriber=acknowledgementSubscriber();
        var upstream=mock(Flow.Subscription.class);
        subscriber.onSubscribe(upstream);
        subscriber.onNext(List.of(ByteBuffer.wrap(new byte[17*1024])));
        var failure=assertThrows(ExecutionException.class,()->subscriber.getBody().toCompletableFuture().get(5,TimeUnit.SECONDS));
        assertInstanceOf(IOException.class,failure.getCause());
        verify(upstream).cancel();
    }
    @Test void acknowledgementSubscriberPreservesSmallChunkedJsonResponses() throws Exception {
        var subscriber=acknowledgementSubscriber();
        var upstream=mock(Flow.Subscription.class);
        subscriber.onSubscribe(upstream);
        subscriber.onNext(List.of(ByteBuffer.wrap("{\"messageId\":".getBytes(StandardCharsets.UTF_8))));
        subscriber.onNext(List.of(ByteBuffer.wrap("\"queued-private-provider-id\"}".getBytes(StandardCharsets.UTF_8))));
        subscriber.onComplete();
        assertEquals("{\"messageId\":\"queued-private-provider-id\"}",subscriber.getBody().toCompletableFuture().get(5,TimeUnit.SECONDS));
        verify(upstream,never()).cancel();
    }
    @Test void disabledProviderNeverContactsNetwork() {
        ReflectionTestUtils.setField(service,"enabled",false);
        assertThrows(ResponseStatusException.class,()->service.sendVerificationEmail("recipient@example.invalid","Synthetic","private-account-token"));
        verifyNoInteractions(client);
    }
    @Test void missingKeyOrSenderFailsBeforeTransport() {
        ReflectionTestUtils.setField(service,"apiKey","");
        assertThrows(ResponseStatusException.class,service::requireConfigured);
        ReflectionTestUtils.setField(service,"apiKey","synthetic-mail-test-only");ReflectionTestUtils.setField(service,"fromEmail","");
        assertThrows(ResponseStatusException.class,service::requireConfigured);verifyNoInteractions(client);
    }
    @Test void invalidSenderAndUnsafeLinkSettingsFailBeforeTransport() {
        ReflectionTestUtils.setField(service,"fromEmail","not-an-email");
        assertThrows(ResponseStatusException.class,service::requireConfigured);
        ReflectionTestUtils.setField(service,"fromEmail","sender@example.invalid");
        ReflectionTestUtils.setField(service,"baseUrl","https://links.example.invalid?unexpected=value");
        assertThrows(ResponseStatusException.class,service::requireConfigured);
        ReflectionTestUtils.setField(service,"baseUrl","https://links.example.invalid");
        ReflectionTestUtils.setField(service,"resetUrl","javascript:invalid");
        assertThrows(ResponseStatusException.class,service::requireConfigured);verifyNoInteractions(client);
    }
    @Test void ioFailureDoesNotExposeProviderMessage() throws Exception {
        when(client.send(any(HttpRequest.class),any(HttpResponse.BodyHandler.class))).thenThrow(new IOException("provider-private-detail"));
        var failure=assertThrows(ResponseStatusException.class,()->service.sendPasswordResetEmail("recipient@example.invalid","Synthetic","private-account-token"));
        assertFalse(failure.getReason().contains("provider-private-detail"));assertFalse(logs.list.isEmpty());
    }
    @Test void timeoutIsReportedAsUnconfirmedWithoutPrivateDetails() throws Exception {
        when(client.send(any(HttpRequest.class),any(HttpResponse.BodyHandler.class))).thenThrow(new HttpTimeoutException("provider-private-detail"));
        assertEquals(503,assertThrows(ResponseStatusException.class,()->service.sendVerificationEmail(
            "recipient@example.invalid","Synthetic","private-account-token")).getStatusCode().value());
    }
    @Test void interruptedTransportRestoresInterruptFlag() throws Exception {
        when(client.send(any(HttpRequest.class),any(HttpResponse.BodyHandler.class))).thenThrow(new InterruptedException("provider-private-detail"));
        assertThrows(ResponseStatusException.class,()->service.sendPasswordResetEmail("recipient@example.invalid","Synthetic","private-account-token"));
        assertTrue(Thread.currentThread().isInterrupted());
    }
    private static String body(HttpRequest request) throws Exception {
        var bytes=new ByteArrayOutputStream();var complete=new CompletableFuture<String>();
        request.bodyPublisher().orElseThrow().subscribe(new Flow.Subscriber<ByteBuffer>() {
            public void onSubscribe(Flow.Subscription subscription) { subscription.request(Long.MAX_VALUE); }
            public void onNext(ByteBuffer buffer) { byte[] chunk=new byte[buffer.remaining()];buffer.get(chunk);bytes.writeBytes(chunk); }
            public void onError(Throwable failure) { complete.completeExceptionally(failure); }
            public void onComplete() { complete.complete(bytes.toString(StandardCharsets.UTF_8)); }
        });
        return complete.get(5,TimeUnit.SECONDS);
    }
}
