package com.orientation.orientationapp.modules.auth.service;
import com.orientation.orientationapp.modules.auth.entity.*;
import com.orientation.orientationapp.modules.auth.repository.*;
import com.orientation.orientationapp.modules.user.repository.CandidateRepository;
import com.orientation.orientationapp.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service @RequiredArgsConstructor
public class EmailVerificationCodeService {
 private final EmailVerificationChallengeRepository challenges;
 private final UserRepository users;
 private final CandidateRepository candidates;
 private final EmailService email;
 @Value("${app.jwt.secret}") private String pepper;
 private final SecureRandom random=new SecureRandom();
 public record Receipt(UUID challengeId,int expiresIn,int resendAfter,String message) {}
 public static Receipt receipt(UUID id) {return new Receipt(id,600,60,"Si votre compte est éligible, consultez votre e-mail pour le code de vérification.");}
 @Transactional
 public UUID issue(User account) {
  User user=users.lockById(account.getId()).orElseThrow(EmailVerificationCodeService::invalid);
  if(Boolean.TRUE.equals(user.getEmailVerified()) || !Boolean.TRUE.equals(user.getEnabled())
      || user.getUserRoles().stream().noneMatch(r->"CANDIDAT".equals(r.getRole().getCode()))) return UUID.randomUUID();
  Instant now=Instant.now();
  EmailVerificationChallenge c=challenges.findByUser_Id(user.getId()).orElseGet(EmailVerificationChallenge::new);
  if(c.getId()!=null && !c.isConsumed() && c.getExpiresAt().isAfter(now)) {
   if(c.getAttempts()>=5 || c.getSendCount()>=3 || c.getLastSentAt().plusSeconds(60).isAfter(now)) return c.getId();
   c.setSendCount(c.getSendCount()+1);
  } else {
   c.setId(c.getId()==null?UUID.randomUUID():c.getId());c.setUser(user);
   c.setExpiresAt(now.plusSeconds(600));c.setAttempts(0);c.setSendCount(1);c.setConsumed(false);
  }
  String code=String.format(Locale.ROOT,"%06d",random.nextInt(1_000_000));
  c.setCodeHash(digest(c.getId(),code));c.setLastSentAt(now);
  challenges.saveAndFlush(c);
  email.sendVerificationCode(user.getEmail(),user.getFirstName(),code);
  return c.getId();
 }
 @Transactional
 public Receipt resend(UUID id) {
  // User lock precedes challenge lock everywhere, preventing verification/resend deadlocks.
  var found=challenges.findById(id);
  if(found.isEmpty()) return receipt(id);
  return receipt(issue(found.get().getUser()));
 }
 @Transactional(noRollbackFor=BusinessException.class)
 public void verify(UUID id,String code) {
  var found=challenges.findById(id).orElseThrow(EmailVerificationCodeService::invalid);
  User user=users.lockById(found.getUser().getId()).orElseThrow(EmailVerificationCodeService::invalid);
  var c=challenges.lockById(id).orElseThrow(EmailVerificationCodeService::invalid);
  if(c.isConsumed() || c.getAttempts()>=5 || !c.getExpiresAt().isAfter(Instant.now())
     || !Boolean.TRUE.equals(user.getEnabled()) || Boolean.TRUE.equals(user.getEmailVerified())) throw invalid();
  c.setAttempts(c.getAttempts()+1);
  boolean matches=code!=null && code.matches("[0-9]{6}") && MessageDigest.isEqual(
      c.getCodeHash().getBytes(StandardCharsets.UTF_8),digest(id,code).getBytes(StandardCharsets.UTF_8));
  if(!matches) {challenges.saveAndFlush(c);throw invalid();}
  c.setConsumed(true);challenges.saveAndFlush(c);
  user.setEmailVerified(true);users.saveAndFlush(user);
  candidates.findByUserId(user.getId()).ifPresent(candidate->{candidate.setVerified(true);candidates.save(candidate);});
 }
 private String digest(UUID id,String code) {
  try {
   Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(pepper.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));
   return HexFormat.of().formatHex(mac.doFinal((id+":"+code).getBytes(StandardCharsets.UTF_8)));
  }catch(GeneralSecurityException e){throw new IllegalStateException("Verification code protection unavailable");}
 }
 private static BusinessException invalid(){return new BusinessException("Code invalide, expiré ou nombre d’essais dépassé.","INVALID_VERIFICATION_CODE");}
}
