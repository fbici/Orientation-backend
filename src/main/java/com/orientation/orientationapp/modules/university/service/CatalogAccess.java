package com.orientation.orientationapp.modules.university.service;
import com.orientation.orientationapp.security.service.ResourceAccess;
import com.orientation.orientationapp.security.service.AccountState;
import com.orientation.orientationapp.security.util.SecurityUtils;
import com.orientation.orientationapp.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class CatalogAccess {
 private final ResourceAccess access;
 private final UserRepository users;
 public UUID tenant() {
  return SecurityUtils.getCurrentUserId().map(UUID::fromString).flatMap(users::findById)
   .filter(AccountState::active).map(u -> u.getTenant().getId()).orElse(null);
 }
 public boolean global() { return tenant()!=null && access.global(); }
 public boolean readable(UUID tenant) { return tenant==null || global() || tenant.equals(tenant()); }
 public void read(UUID tenant) { if(!readable(tenant)) throw ResourceAccess.denied(); }
 public void write(UUID tenant,String permission) {
  access.require(permission);
  if(tenant==null ? !access.global() : !tenant.equals(tenant()) && !access.global()) throw ResourceAccess.denied();
 }
 public boolean manages(String permission) { return tenant()!=null && access.authority(permission); }
 public UUID create(String permission) { access.require(permission);return access.current().getTenant().getId(); }
}
