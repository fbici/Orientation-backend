package com.orientation.orientationapp.security;

import com.orientation.orientationapp.backoffice.notification_center.service.*;
import com.orientation.orientationapp.backoffice.notification_center.service.impl.NotificationServiceImpl;
import com.orientation.orientationapp.backoffice.notification_center.repository.NotificationRepository;
import com.orientation.orientationapp.backoffice.notification_center.entity.Notification;
import com.orientation.orientationapp.intelligence.pipeline.DocumentProcessingPipeline.PipelineProgressEvent;
import com.orientation.orientationapp.modules.auth.service.EmailService;
import com.orientation.orientationapp.security.service.ResourceAccess;
import org.junit.jupiter.api.*;
import org.springframework.context.*;
import org.springframework.context.annotation.*;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.*;
import org.springframework.transaction.support.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(AdministrativeEmailTest.Config.class)
@org.springframework.test.context.ActiveProfiles("administrative-email-unit")
class AdministrativeEmailTest {
 static final UUID ADMIN=UUID.fromString("00000000-0000-0000-0000-000000000001");
 @org.springframework.boot.test.context.TestConfiguration
 @Profile("administrative-email-unit")
 @org.springframework.transaction.annotation.EnableTransactionManagement
 static class Config {
  @Bean AdministrativeRecipientPolicy policy(){return mock(AdministrativeRecipientPolicy.class);}
  @Bean EmailService email(){return mock(EmailService.class);}
  @Bean AdministrativeEmailListener listener(AdministrativeRecipientPolicy p,EmailService e){
   return new AdministrativeEmailListener(p,e,Runnable::run);
  }
  @Bean PlatformTransactionManager manager(){return new AbstractPlatformTransactionManager(){
   protected Object doGetTransaction(){return new Object();}
   protected void doBegin(Object t,TransactionDefinition d){}
   protected void doCommit(DefaultTransactionStatus s){}
   protected void doRollback(DefaultTransactionStatus s){}
  };}
 }
 @Autowired AdministrativeRecipientPolicy policy;
 @Autowired EmailService email;
 @Autowired AdministrativeEmailListener listener;
 @Autowired ApplicationEventPublisher events;
 @Autowired PlatformTransactionManager manager;
 @BeforeEach void setup(){reset(policy,email);when(policy.isAdministrator(ADMIN)).thenReturn(true);
  ReflectionTestUtils.setField(listener,"enabled",true);
  ReflectionTestUtils.setField(listener,"recipient","adminorientia@gmail.com");
  ReflectionTestUtils.setField(listener,"baseUrl","https://site.example.invalid");
 }
 @Test void notificationIsSentOnlyAfterCommit(){
  new TransactionTemplate(manager).execute(s->{
   events.publishEvent(new AdministrativeNotificationCreated(UUID.randomUUID(),ADMIN.toString()));
   verifyNoInteractions(email);return null;
  });
  verify(email).sendAdministrativeNotification(eq("adminorientia@gmail.com"),contains("/auth/login?returnUrl=%2Fnotifications"));
 }
 @Test void rollbackDoesNotSend(){
  new TransactionTemplate(manager).execute(s->{events.publishEvent(new AdministrativeNotificationCreated(UUID.randomUUID(),ADMIN.toString()));s.setRollbackOnly();return null;});
  verifyNoInteractions(email);
 }
 @Test void candidateAndMissingOwnerAreNotForwarded(){
  listener.notification(new AdministrativeNotificationCreated(UUID.randomUUID(),UUID.randomUUID().toString()));
  listener.notification(new AdministrativeNotificationCreated(UUID.randomUUID(),null));
  verifyNoInteractions(email);
 }
 @Test void pipelineSendsOnlyTerminalEventsWithoutPrivateContent(){
  listener.pipeline(new PipelineProgressEvent(UUID.randomUUID(),"OCR","private-document-body",ADMIN));
  verifyNoInteractions(email);
  listener.pipeline(new PipelineProgressEvent(UUID.randomUUID(),"READY_REVIEW","private-document-body",ADMIN));
  listener.pipeline(new PipelineProgressEvent(UUID.randomUUID(),"FAILED","private-account-token",ADMIN));
  var body=org.mockito.ArgumentCaptor.forClass(String.class);
  verify(email,times(2)).sendAdministrativeNotification(eq("adminorientia@gmail.com"),body.capture());
  assertTrue(body.getAllValues().stream().noneMatch(x->x.contains("private-")));
 }
 @Test void providerFailureNeverBreaksTheBusinessOperation(){
  doThrow(new IllegalStateException("private-provider-diagnostic")).when(email).sendAdministrativeNotification(anyString(),anyString());
  assertDoesNotThrow(()->listener.pipeline(new PipelineProgressEvent(UUID.randomUUID(),"FAILED","private-data",ADMIN)));
 }
 @Test void disabledAlertsDoNotCallProvider(){
  ReflectionTestUtils.setField(listener,"enabled",false);
  listener.pipeline(new PipelineProgressEvent(UUID.randomUUID(),"FAILED","private-data",ADMIN));
  verifyNoInteractions(email);
 }
 @Test void saturatedQueueDoesNotBreakDocumentProcessing(){
  var full=new AdministrativeEmailListener(policy,email,task->{throw new RejectedExecutionException();});
  ReflectionTestUtils.setField(full,"enabled",true);
  assertDoesNotThrow(()->full.pipeline(new PipelineProgressEvent(UUID.randomUUID(),"FAILED","private-data",ADMIN)));
 }
 @Test void notificationCreationPublishesAnEventWithoutItsPrivateBody(){
  var repo=mock(NotificationRepository.class);var publisher=mock(ApplicationEventPublisher.class);
  var saved=Notification.builder().userId(ADMIN.toString()).build();saved.setId(UUID.randomUUID());
  when(repo.save(any())).thenReturn(saved);
  var service=new NotificationServiceImpl(repo,publisher,mock(ResourceAccess.class));
  service.create("INFO","private-title","private-account-token",ADMIN.toString());
  verify(publisher).publishEvent(new AdministrativeNotificationCreated(saved.getId(),ADMIN.toString()));
 }
}
