package com.orientation.orientationapp.security;
import com.orientation.orientationapp.backoffice.notification_center.service.AdministrativeRecipientPolicy;
import com.orientation.orientationapp.modules.auth.entity.*;
import com.orientation.orientationapp.modules.auth.repository.UserRepository;
import com.orientation.orientationapp.common.enums.CandidateStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class AdministrativeRecipientPolicyTest {
 private final UserRepository users=mock(UserRepository.class);
 private final AdministrativeRecipientPolicy policy=new AdministrativeRecipientPolicy(users);
 private final UUID id=UUID.randomUUID();
 private User account(String code) {
  var org=new Organization();org.setActive(true);
  var tenant=new Tenant();tenant.setActive(true);tenant.setOrganization(org);
  var user=new User();user.setEnabled(true);user.setEmailVerified(true);user.setStatus(CandidateStatus.ACTIVE);user.setTenant(tenant);
  var role=new Role();role.setCode(code);var membership=new UserRole();membership.setRole(role);membership.setUser(user);
  user.setUserRoles(new HashSet<>(List.of(membership)));when(users.findById(id)).thenReturn(Optional.of(user));return user;
 }
 @ParameterizedTest @ValueSource(strings={"ADMIN","SUPER_ADMIN"})
 void acceptsOnlyAnActiveAdministrativeRole(String code){account(code);assertTrue(policy.isAdministrator(id));}
 @Test void candidateIsNotAnAdministrativeRecipient(){account("CANDIDAT");assertFalse(policy.isAdministrator(id));}
 @Test void lockedAdministratorIsNotEligible(){var u=account("ADMIN");u.setAccountLocked(true);assertFalse(policy.isAdministrator(id));}
 @Test void suspendedTenantIsNotEligible(){var u=account("ADMIN");u.getTenant().setActive(false);assertFalse(policy.isAdministrator(id));}
}
