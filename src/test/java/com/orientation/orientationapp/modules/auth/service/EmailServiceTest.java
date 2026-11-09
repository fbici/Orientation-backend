package com.orientation.orientationapp.modules.auth.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
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
import java.util.concurrent.CompletableFuture;
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
        ReflectionTestUtils.setField(service,"baseUrl","https://links.example.invalid");
        ReflectionTestUtils.setField(service,"resetUrl","");
        logger=(Logger)LoggerFactory.getLogger(EmailService.class);
        logs=new ListAppender<>();logs.start();logger.addAppender(logs);
    }
    @AfterEach void cleanup() {
        logger.detachAppender(logs);logs.stop();Thread.interrupted();
        String text=logs.list.stream().map(ILoggingEvent::getFormattedMessage).reduce("",String::concat);
        for(String privateValue:new String[]{"synthetic-mail-test-only","private-account-token","recipient@example.invalid","provider-private-detail"})
            assertFalse(text.contains(privateValue));
    }
    @SuppressWarnings("unchecked")
    private void respond(int status) throws Exception {
        HttpResponse<Void> response=mock(HttpResponse.class);when(response.statusCode()).thenReturn(status);
        when(client.send(any(HttpRequest.class),any(HttpResponse.BodyHandler.class))).thenReturn(response);
    }
    @Test void acceptedRequestUsesExistingProviderAndVerificationLink() throws Exception {
        respond(202);
        service.sendVerificationEmail("recipient@example.invalid","Synthetic","private-account-token");
        var request=ArgumentCaptor.forClass(HttpRequest.class);
        verify(client).send(request.capture(),any(HttpResponse.BodyHandler.class));
        assertEquals("https://api.sendgrid.com/v3/mail/send",request.getValue().uri().toString());
        assertEquals("Bearer synthetic-mail-test-only",request.getValue().headers().firstValue("Authorization").orElseThrow());
        var json=new ObjectMapper().readTree(body(request.getValue()));
        assertEquals("sender@example.invalid",json.path("from").path("email").asText());
        assertTrue(json.path("content").get(0).path("value").asText().contains("https://links.example.invalid/auth/verify-email?token=private-account-token"));
    }
    @Test void resetUsesConfiguredPage() throws Exception {
        respond(202);ReflectionTestUtils.setField(service,"resetUrl","https://links.example.invalid/auth/reset-password");
        service.sendPasswordResetEmail("recipient@example.invalid","Synthetic","private-account-token");
        var request=ArgumentCaptor.forClass(HttpRequest.class);verify(client).send(request.capture(),any(HttpResponse.BodyHandler.class));
        assertTrue(body(request.getValue()).contains("https://links.example.invalid/auth/reset-password?token=private-account-token"));
    }
    @ParameterizedTest @ValueSource(ints={400,401,429,500})
    void providerFailureIsLoggedWithoutPrivateDetails(int status) throws Exception {
        respond(status);
        var failure=assertThrows(ResponseStatusException.class,()->service.sendVerificationEmail("recipient@example.invalid","Synthetic","private-account-token"));
        assertEquals(503,failure.getStatusCode().value());
        assertTrue(logs.list.stream().anyMatch(e->e.getFormattedMessage().contains("HTTP "+status)));
        assertFalse(failure.getReason().contains("private-account-token"));
    }
    @Test void productionYamlBindsTheSendGridEnvironmentVariables() {
        new org.springframework.boot.test.context.runner.ApplicationContextRunner()
            .withInitializer(context -> {
                var sources=context.getEnvironment().getPropertySources();
                sources.addFirst(new org.springframework.core.env.MapPropertySource("synthetic-env", java.util.Map.of(
                    "SENDGRID_ENABLED","true", "SENDGRID_API_KEY","synthetic-mail-test-only",
                    "SENDGRID_FROM_EMAIL","sender@example.invalid", "APP_BASE_URL","https://links.example.invalid")));
                try {
                    for(var source:new org.springframework.boot.env.YamlPropertySourceLoader().load("actual-prod",
                            new org.springframework.core.io.ClassPathResource("application-prod.yml"))) sources.addLast(source);
                } catch(IOException ex) { throw new IllegalStateException(ex); }
            })
            .withBean(EmailService.class, () -> new EmailService(new ObjectMapper(),client))
            .run(context -> {
                assertNull(context.getStartupFailure());
                context.getBean(EmailService.class).requireConfigured();
                verifyNoInteractions(client);
            });
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
    @Test void ioFailureDoesNotExposeProviderMessage() throws Exception {
        when(client.send(any(HttpRequest.class),any(HttpResponse.BodyHandler.class))).thenThrow(new IOException("provider-private-detail"));
        var failure=assertThrows(ResponseStatusException.class,()->service.sendPasswordResetEmail("recipient@example.invalid","Synthetic","private-account-token"));
        assertFalse(failure.getReason().contains("provider-private-detail"));assertFalse(logs.list.isEmpty());
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
