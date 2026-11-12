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
    private final InternationalPhoneService phones;
    private final UserRepository users;private final TenantRepository tenants;
    private final RoleRepository roles;private final CandidateRepository candidates;
    private final PasswordEncoder encoder;private final AccountRecoveryService recovery;private final EmailService email;
    private final EmailVerificationCodeService codes;
    @Value("${app.registration.tenant-code:DEFAULT}") private String publicTenant;
    @Transactional
    public void register(RegisterRequest request) { register(request,false); }
    @Transactional
    public java.util.UUID registerMobile(RegisterRequest request) {return register(request,true);}
    private java.util.UUID register(RegisterRequest request,boolean mobile) {
        String normalizedPhone=phones.normalize(request.getPhone());
        // Do not issue a fictitious mobile challenge for a disabled/deleted account.
        // This explicit unavailable-address outcome is a product decision; it reveals no deletion reason.
        if(mobile && users.countDeletionByEmail(request.getEmail())>0)
            throw new BusinessException("La suppression de ce compte a été demandée. L’adresse reste réservée pendant 30 jours et jusqu’à la fin de l’effacement. Aucun code n’a été envoyé.","ACCOUNT_DELETION_PENDING");
        if(mobile && users.countInactiveByEmail(request.getEmail())>0)
            throw new BusinessException("Cette adresse est associée à un compte désactivé. Une nouvelle inscription avec cette adresse est indisponible. Aucun code n’a été envoyé. Contactez le support Orientia.","ACCOUNT_INACTIVE");
        email.requireConfigured();
        if(request.getTenantCode()!=null && !publicTenant.equalsIgnoreCase(request.getTenantCode()))
            throw new BusinessException("Public registration does not allow this tenant","INVALID_REGISTRATION_TENANT");
        if(request.getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
            throw new BusinessException("Password exceeds 72 bytes","INVALID_PASSWORD");
        if(users.countAllByEmail(request.getEmail())>0) {
            var existing=users.findByEmail(request.getEmail())
                .filter(u -> !Boolean.TRUE.equals(u.getEmailVerified()) && Boolean.TRUE.equals(u.getEnabled())
                    && u.getTenant()!=null && publicTenant.equalsIgnoreCase(u.getTenant().getCode()))
                ;
            if(mobile) return existing.map(codes::issue).orElseGet(java.util.UUID::randomUUID);
            existing.ifPresent(recovery::verificationEmail);
            return java.util.UUID.randomUUID();
        }
        Tenant tenant=tenants.findByCode(publicTenant).filter(t -> Boolean.TRUE.equals(t.getActive()))
            .orElseThrow(() -> new BusinessException("Public registration is not configured","REGISTRATION_UNAVAILABLE"));
        Role role=roles.findByCode("CANDIDAT").orElseThrow(() -> new IllegalStateException("Candidate role is not configured"));
        User user=new User();user.setEmail(request.getEmail());user.setPassword(encoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());user.setLastName(request.getLastName());user.setPhone(normalizedPhone);
        user.setTenant(tenant);user.setStatus(CandidateStatus.ACTIVE);user.setEmailVerified(false);
        UserRole membership=new UserRole();membership.setUser(user);membership.setRole(role);user.getUserRoles().add(membership);
        users.saveAndFlush(user);
        Candidate candidate=new Candidate();candidate.setEmail(user.getEmail());candidate.setFirstName(user.getFirstName());
        candidate.setLastName(user.getLastName());candidate.setPhone(user.getPhone());candidate.setUser(user);candidate.setTenant(tenant);
        candidates.saveAndFlush(candidate);
        if(mobile) return codes.issue(user);
        recovery.verificationEmail(user);return java.util.UUID.randomUUID();
    }
}
