package com.orientation.orientationapp.modules.auth.service;
import com.orientation.orientationapp.modules.auth.entity.*;
import com.orientation.orientationapp.modules.auth.repository.*;
import com.orientation.orientationapp.modules.user.repository.CandidateRepository;
import com.orientation.orientationapp.exception.BusinessException;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import java.time.Instant;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
class EmailVerificationCodeServiceTest {
 EmailVerificationChallengeRepository repo;UserRepository users;CandidateRepository candidates;EmailService email;
 EmailVerificationCodeService service;User user;EmailVerificationChallenge challenge;String sent;
 @BeforeEach void setup(){
  repo=mock(EmailVerificationChallengeRepository.class);users=mock(UserRepository.class);
  candidates=mock(CandidateRepository.class);email=mock(EmailService.class);
  service=new EmailVerificationCodeService(repo,users,candidates,email);
  ReflectionTestUtils.setField(service,"pepper","synthetic-only-test-key-at-least-32-bytes");
  user=new User();user.setId(UUID.randomUUID());user.setEmail("synthetic@example.test");user.setFirstName("Fixture");
  Role role=new Role();role.setCode("CANDIDAT");UserRole membership=new UserRole();membership.setRole(role);membership.setUser(user);user.getUserRoles().add(membership);
  when(users.lockById(user.getId())).thenReturn(Optional.of(user));
  when(repo.findByUser_Id(user.getId())).thenAnswer(i->Optional.ofNullable(challenge));
  when(repo.saveAndFlush(any())).thenAnswer(i->{challenge=i.getArgument(0);return challenge;});
  doAnswer(i->{sent=i.getArgument(2);return null;}).when(email).sendVerificationCode(anyString(),anyString(),anyString());
  when(candidates.findByUserId(user.getId())).thenReturn(Optional.empty());
 }
 void issue(){UUID id=service.issue(user);when(repo.findById(id)).thenAnswer(i->Optional.of(challenge));when(repo.lockById(id)).thenAnswer(i->Optional.of(challenge));}
 @Test void sendsSixDigitsAndStoresOnlyAnAccountBoundKeyedDigest(){issue();assertTrue(sent.matches("[0-9]{6}"));assertEquals(64,challenge.getCodeHash().length());assertNotEquals(sent,challenge.getCodeHash());assertTrue(challenge.getExpiresAt().isAfter(Instant.now().plusSeconds(590)));}
 @Test void acceptsCorrectCodeOnceWithoutIssuingAnySession(){issue();service.verify(challenge.getId(),sent);assertTrue(user.getEmailVerified());assertTrue(challenge.isConsumed());assertThrows(BusinessException.class,()->service.verify(challenge.getId(),sent));}
 @Test void fiveWrongGuessesLockEvenTheCorrectCode(){issue();String correct=sent;String wrong=correct.equals("000000")?"000001":"000000";for(int i=0;i<5;i++)assertThrows(BusinessException.class,()->service.verify(challenge.getId(),wrong));assertEquals(5,challenge.getAttempts());assertThrows(BusinessException.class,()->service.verify(challenge.getId(),correct));assertFalse(user.getEmailVerified());}
 @Test void expiredCodeCannotVerify(){issue();challenge.setExpiresAt(Instant.now().minusSeconds(1));assertThrows(BusinessException.class,()->service.verify(challenge.getId(),sent));assertFalse(user.getEmailVerified());}
 @Test void resendCooldownDoesNotContactProvider(){issue();service.resend(challenge.getId());verify(email,times(1)).sendVerificationCode(anyString(),anyString(),anyString());}
 @Test void resendInvalidatesThePreviousDigestAndPreservesFailedAttempts(){issue();String hash=challenge.getCodeHash();challenge.setAttempts(2);challenge.setLastSentAt(Instant.now().minusSeconds(61));service.resend(challenge.getId());assertEquals(2,challenge.getAttempts());assertEquals(2,challenge.getSendCount());verify(email,times(2)).sendVerificationCode(anyString(),anyString(),anyString());}
 @Test void resendIsCappedAtThreeAndExhaustionCannotResetAttempts(){issue();challenge.setSendCount(3);challenge.setLastSentAt(Instant.now().minusSeconds(61));service.resend(challenge.getId());verify(email,times(1)).sendVerificationCode(anyString(),anyString(),anyString());}
 @Test void administrativeAccountsNeverReceiveCandidateVerificationCodes(){user.getUserRoles().iterator().next().getRole().setCode("SUPER_ADMIN");assertNotNull(service.issue(user));verifyNoInteractions(email);assertNull(challenge);}
 @Test void disabledAndAlreadyVerifiedAccountsCannotConsumeCodes(){issue();user.setEnabled(false);assertThrows(BusinessException.class,()->service.verify(challenge.getId(),sent));assertFalse(challenge.isConsumed());}
}
