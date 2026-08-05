package com.orientation.orientationapp.config;

import com.orientation.orientationapp.common.enums.CandidateStatus;
import com.orientation.orientationapp.modules.auth.entity.*;
import com.orientation.orientationapp.modules.auth.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

/**
 * Cree les donnees de base au premier demarrage en production :
 * - Tenant par defaut
 * - Roles (SUPER_ADMIN, ADMIN, CANDIDAT)
 * - Utilisateur admin par defaut
 */
@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class ProdDataInitializer implements ApplicationRunner {

    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 1. Tenant par defaut
        Tenant tenant = findOrCreateTenant();

        // 2. Roles
        Role superAdmin = findOrCreateRole("SUPER_ADMIN", "Super Administrateur", tenant);
        Role admin = findOrCreateRole("ADMIN", "Administrateur", tenant);
        Role candidat = findOrCreateRole("CANDIDAT", "Candidat", tenant);

        // 3. Permissions de base
        createPermissionsIfNotExist();

        // 4. Admin par defaut
        if (userRepository.findByEmail("admin@orientia.com").isEmpty()) {
            User adminUser = new User();
            adminUser.setEmail("admin@orientia.com");
            adminUser.setPassword(passwordEncoder.encode("Admin@2025"));
            adminUser.setFirstName("Super");
            adminUser.setLastName("Admin");
            adminUser.setTenant(tenant);
            adminUser.setStatus(CandidateStatus.ACTIVE);
            adminUser.setEmailVerified(true);
            adminUser.setEnabled(true);
            adminUser.setAccountLocked(false);
            adminUser.setFailedLoginAttempts(0);
            adminUser.setMfaEnabled(false);

            UserRole ur = new UserRole();
            ur.setUser(adminUser);
            ur.setRole(superAdmin);
            adminUser.getUserRoles().add(ur);

            userRepository.save(adminUser);
            log.info("Default admin created: admin@orientia.com / Admin@2025");
        } else {
            log.info("Default admin already exists");
        }
    }

    private Tenant findOrCreateTenant() {
        Optional<Tenant> existing = tenantRepository.findByCode("default");
        if (existing.isPresent()) return existing.get();

        Tenant tenant = new Tenant();
        tenant.setName("Orientia Platform");
        tenant.setCode("default");
        tenant.setActive(true);
        Tenant saved = tenantRepository.save(tenant);
        log.info("Default tenant created");
        return saved;
    }

    private Role findOrCreateRole(String code, String name, Tenant tenant) {
        Optional<Role> existing = roleRepository.findByCode(code);
        if (existing.isPresent()) return existing.get();

        Role role = new Role();
        role.setCode(code);
        role.setName(name);
        role.setDescription(name);
        role.setTenant(tenant);
        role.setActive(true);
        Role saved = roleRepository.save(role);
        log.info("Role created: {}", code);
        return saved;
    }

    private void createPermissionsIfNotExist() {
        String[] perms = {
            "users.read", "users.write", "users.delete",
            "roles.read", "roles.write",
            "universities.read", "universities.write",
            "candidates.read", "candidates.write",
            "recommendations.read", "recommendations.write",
            "documents.read", "documents.write",
            "settings.read", "settings.write",
            "dashboard.read", "analytics.read"
        };
        for (String p : perms) {
            if (permissionRepository.findByCode(p).isEmpty()) {
                Permission perm = new Permission();
                perm.setCode(p);
                perm.setName(p.replace(".", " "));
                perm.setDescription(p);
                permissionRepository.save(perm);
            }
        }
    }
}
