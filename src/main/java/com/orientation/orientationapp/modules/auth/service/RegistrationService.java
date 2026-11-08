package com.orientation.orientationapp.modules.auth.service;
import com.orientation.orientationapp.modules.auth.dto.request.RegisterRequest;
import com.orientation.orientationapp.modules.auth.entity.*;
import com.orientation.orientationapp.modules.auth.repository.*;
import com.orientation.orientationapp.modules.user.entity.Candidate;
import com.orientation.orientationapp.modules.user.repository.CandidateRepository;
import com.orientation.orientationapp.common.enums.CandidateStatus;
import com.orientation.orientationapp.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
@Service @RequiredArgsConstructor
public class RegistrationService {
    private final UserRepository users;private final TenantRepository tenants;
    private final RoleRepository roles;private final CandidateRepository candidates;
    private final PasswordEncoder encoder;private final AccountRecoveryService recovery;private final EmailService email;
    @Value("${app.registration.tenant-code:DEFAULT}") private String publicTenant;
    @Transactional
    public void register(RegisterRequest request) {
        email.requireConfigured();
        if(request.getTenantCode()!=null && !publicTenant.equalsIgnoreCase(request.getTenantCode()))
            throw new BusinessException("Public registration does not allow this tenant","INVALID_REGISTRATION_TENANT");
        if(request.getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
            throw new BusinessException("Password exceeds 72 bytes","INVALID_PASSWORD");
        if(users.countAllByEmail(request.getEmail())>0) {
            users.findByEmail(request.getEmail())
                .filter(u -> !Boolean.TRUE.equals(u.getEmailVerified()) && Boolean.TRUE.equals(u.getEnabled())
                    && u.getTenant()!=null && publicTenant.equalsIgnoreCase(u.getTenant().getCode()))
                .ifPresent(recovery::verificationEmail);
            return;
        }
        Tenant tenant=tenants.findByCode(publicTenant).filter(t -> Boolean.TRUE.equals(t.getActive()))
            .orElseThrow(() -> new BusinessException("Public registration is not configured","REGISTRATION_UNAVAILABLE"));
        Role role=roles.findByCode("CANDIDAT").orElseThrow(() -> new IllegalStateException("Candidate role is not configured"));
        User user=new User();user.setEmail(request.getEmail());user.setPassword(encoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());user.setLastName(request.getLastName());user.setPhone(request.getPhone());
        user.setTenant(tenant);user.setStatus(CandidateStatus.ACTIVE);user.setEmailVerified(false);
        UserRole membership=new UserRole();membership.setUser(user);membership.setRole(role);user.getUserRoles().add(membership);
        users.saveAndFlush(user);
        Candidate candidate=new Candidate();candidate.setEmail(user.getEmail());candidate.setFirstName(user.getFirstName());
        candidate.setLastName(user.getLastName());candidate.setPhone(user.getPhone());candidate.setUser(user);candidate.setTenant(tenant);
        candidates.saveAndFlush(candidate);recovery.verificationEmail(user);
    }
}
