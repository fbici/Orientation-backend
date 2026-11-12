package com.orientation.orientationapp.modules.auth.service;
import com.orientation.orientationapp.modules.auth.repository.*;
import com.orientation.orientationapp.modules.user.repository.CandidateRepository;
import com.orientation.orientationapp.modules.auth.dto.request.RegisterRequest;
import com.orientation.orientationapp.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class RegistrationAvailabilityTest {
 @Test void disabledAccountNeverGetsAnAcceptedChallengeOrAnEmail() {
  var users=mock(UserRepository.class);var codes=mock(EmailVerificationCodeService.class);var email=mock(EmailService.class);
  var recovery=mock(AccountRecoveryService.class);
  var service=new RegistrationService(mock(InternationalPhoneService.class),users,mock(TenantRepository.class),mock(RoleRepository.class),mock(CandidateRepository.class),mock(PasswordEncoder.class),recovery,email,codes);
  ReflectionTestUtils.setField(service,"publicTenant","DEFAULT");
  var request=new RegisterRequest();request.setEmail("disabled@example.invalid");request.setPassword("SyntheticPassword!123");
  when(users.countInactiveByEmail(request.getEmail())).thenReturn(1L);
  var failure=assertThrows(BusinessException.class,()->service.registerMobile(request));
  assertTrue(failure.getMessage().contains("Aucun code"));
  verifyNoInteractions(email,codes,recovery);
  verify(users,never()).saveAndFlush(any());
 }
 @Test void verifiedActiveAccountKeepsItsExistingNeutralResponse() {
  var users=mock(UserRepository.class);var codes=mock(EmailVerificationCodeService.class);var email=mock(EmailService.class);
  var service=new RegistrationService(mock(InternationalPhoneService.class),users,mock(TenantRepository.class),mock(RoleRepository.class),mock(CandidateRepository.class),mock(PasswordEncoder.class),mock(AccountRecoveryService.class),email,codes);
  ReflectionTestUtils.setField(service,"publicTenant","DEFAULT");
  var request=new RegisterRequest();request.setEmail("active@example.invalid");request.setPassword("SyntheticPassword!123");
  when(users.countAllByEmail(request.getEmail())).thenReturn(1L);
  assertNotNull(service.registerMobile(request));verifyNoInteractions(codes);verify(email).requireConfigured();
 }
 @Test void explicitNullPhoneIsReturnedForClientsThatNeedSafeProfileEditing() throws Exception {
  var user=new com.orientation.orientationapp.modules.auth.dto.response.UserInfo();
  var mapper=new com.fasterxml.jackson.databind.ObjectMapper().setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
  var data=mapper.readTree(mapper.writeValueAsString(user));
  assertTrue(data.has("phone"));assertTrue(data.get("phone").isNull());
  user.setPhone("+2290197000000");assertEquals(user.getPhone(),mapper.valueToTree(user).get("phone").asText());
 }
}
