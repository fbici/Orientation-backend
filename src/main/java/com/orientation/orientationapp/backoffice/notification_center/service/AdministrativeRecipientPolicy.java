package com.orientation.orientationapp.backoffice.notification_center.service;
import com.orientation.orientationapp.modules.auth.repository.UserRepository;
import com.orientation.orientationapp.security.service.AccountState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.util.Set;
@Service @RequiredArgsConstructor
public class AdministrativeRecipientPolicy {
 private final UserRepository users;
 @Transactional(readOnly=true)
 public boolean isAdministrator(UUID userId) {
  if(userId==null) return false;
  return users.findById(userId).filter(AccountState::active).map(user ->
   user.getUserRoles().stream().anyMatch(m -> m.getRole()!=null &&
    Set.of("ADMIN","SUPER_ADMIN").contains(m.getRole().getCode()))).orElse(false);
 }
}
