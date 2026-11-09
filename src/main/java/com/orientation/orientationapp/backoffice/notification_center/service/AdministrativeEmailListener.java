package com.orientation.orientationapp.backoffice.notification_center.service;
import com.orientation.orientationapp.intelligence.pipeline.DocumentProcessingPipeline.PipelineProgressEvent;
import com.orientation.orientationapp.modules.auth.service.EmailService;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.*;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
@Component @Slf4j
public class AdministrativeEmailListener {
 private final AdministrativeRecipientPolicy policy;
 private final EmailService email;
 private final Executor executor;
 @Value("${app.notifications.admin-email:adminorientia@gmail.com}") private String recipient;
 @Value("${app.notifications.admin-email-enabled:true}") private boolean enabled;
 @Value("${app.base-url:}") private String baseUrl;
 public AdministrativeEmailListener(AdministrativeRecipientPolicy policy,EmailService email,
   @Qualifier("administrativeEmailExecutor") Executor executor) {
  this.policy=policy;this.email=email;this.executor=executor;
 }
 @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT,fallbackExecution=true)
 public void notification(AdministrativeNotificationCreated event) {
  UUID owner;
  try { owner=UUID.fromString(event.userId()); } catch(IllegalArgumentException|NullPointerException ex) { return; }
  enqueue(owner,"Une nouvelle notification administrative est disponible.","/notifications");
 }
 @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT,fallbackExecution=true)
 public void pipeline(PipelineProgressEvent event) {
  if(!Set.of("READY_REVIEW","FAILED").contains(event.step())) return;
  String message="FAILED".equals(event.step())
    ? "Un traitement documentaire nécessite une intervention administrative."
    : "Un document est prêt pour la validation administrative.";
  enqueue(event.userId(),message,"/documents");
 }
 private void enqueue(UUID owner,String message,String page) {
  if(!enabled) return;
  try { executor.execute(() -> {
   try {
    if(!policy.isAdministrator(owner)) return;
    if(recipient==null || !recipient.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
     { log.warn("Administrative email not sent: recipient configuration invalid");return; }
    email.sendAdministrativeNotification(recipient,message+"\nConsultez votre espace Orientia : "+baseUrl+"/auth/login?returnUrl="+java.net.URLEncoder.encode(page,java.nio.charset.StandardCharsets.UTF_8));
   } catch(RuntimeException ex) {
    log.warn("Administrative email delivery failed; delivery is unconfirmed");
   }
  }); } catch(RejectedExecutionException ex) {
   log.warn("Administrative email queue full; delivery is unconfirmed");
  }
 }
}
