package com.orientation.orientationapp.config;

import com.orientation.orientationapp.common.enums.CandidateStatus;
import com.orientation.orientationapp.modules.auth.entity.*;
import com.orientation.orientationapp.modules.auth.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Explicit, one-time bootstrap. Never changes an existing account. */
@Component
@ConditionalOnProperty(name = "app.bootstrap.enabled", havingValue = "true")
@RequiredArgsConstructor
public class ProdDataInitializer implements ApplicationRunner {
    private final TenantRepository tenants;
    private final RoleRepository roles;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Value("${app.bootstrap.email:}") private String email;
    @Value("${app.bootstrap.password:}") private String password;
    @Value("${app.bootstrap.tenant-code:}") private String tenantCode;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Native query deliberately includes soft-deleted accounts.
        if (users.countAllSuperAdministrators() > 0)
            throw new IllegalStateException("Bootstrap refused: administrator already provisioned");
        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") ||
                password == null || password.length() < 16 || password.length() > 72 ||
                !password.matches("(?s).*[A-Z].*") || !password.matches("(?s).*[a-z].*") ||
                !password.matches("(?s).*[0-9].*") || !password.matches("(?s).*[^a-zA-Z0-9].*") ||
                tenantCode == null || tenantCode.isBlank())
            throw new IllegalStateException("Bootstrap requires an email, existing tenant and strong secret (16-72 characters)");
        if (users.countAllByEmail(email) > 0)
            throw new IllegalStateException("Bootstrap refused: account already provisioned");
        Tenant tenant = tenants.findByCode(tenantCode)
                .filter(t -> Boolean.TRUE.equals(t.getActive()))
                .orElseThrow(() -> new IllegalStateException("Bootstrap requires an active existing tenant"));
        Role role = roles.lockByCode("SUPER_ADMIN")
                .orElseThrow(() -> new IllegalStateException("Apply role migrations before bootstrap"));
        if (users.countAllSuperAdministrators() > 0 || users.countAllByEmail(email) > 0)
            throw new IllegalStateException("Bootstrap refused: account already provisioned");
        User user = new User();
        user.setEmail(email);
        user.setFirstName("Platform");
        user.setLastName("Administrator");
        user.setPassword(encoder.encode(password));
        user.setTenant(tenant);
        user.setStatus(CandidateStatus.ACTIVE);
        user.setEnabled(true);
        user.setEmailVerified(true);
        UserRole membership = new UserRole();
        membership.setUser(user);
        membership.setRole(role);
        user.getUserRoles().add(membership);
        users.save(user);
    }
}
