package com.orientation.orientationapp.modules.auth.controller;

import com.orientation.orientationapp.common.enums.CandidateStatus;
import com.orientation.orientationapp.common.enums.UserRole;
import com.orientation.orientationapp.modules.auth.dto.request.RegisterRequest;
import com.orientation.orientationapp.modules.auth.dto.response.RegisterResponse;
import com.orientation.orientationapp.modules.auth.entity.Role;
import com.orientation.orientationapp.modules.auth.entity.Tenant;
import com.orientation.orientationapp.modules.auth.entity.User;
import com.orientation.orientationapp.modules.auth.repository.RoleRepository;
import com.orientation.orientationapp.modules.auth.repository.TenantRepository;
import com.orientation.orientationapp.modules.auth.repository.UserRepository;
import com.orientation.orientationapp.modules.user.entity.Candidate;
import com.orientation.orientationapp.modules.user.repository.CandidateRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Endpoint d'inscription publique pour les candidats.
 *
 * POST /api/v1/auth/register
 * Crée un utilisateur + un profil candidat + assigne le role CANDIDAT
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class RegisterController {

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());

        // 1. Verifier si l'email existe deja
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(
                RegisterResponse.builder()
                    .message("Cet email est deja utilise")
                    .build()
            );
        }

        // 2. Trouver ou creer le tenant
        Tenant tenant = findOrCreateTenant(request.getTenantCode());

        // 3. Creer l'utilisateur
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setTenant(tenant);
        user.setStatus(CandidateStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setEnabled(true);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setMfaEnabled(false);

        user = userRepository.save(user);
        log.info("User created: {} ({})", user.getEmail(), user.getId());

        // 4. Assigner le role CANDIDAT
        Optional<Role> candidateRole = roleRepository.findByCode("CANDIDAT");
        if (candidateRole.isPresent()) {
            com.orientation.orientationapp.modules.auth.entity.UserRole ur = new com.orientation.orientationapp.modules.auth.entity.UserRole();
            ur.setUser(user);
            ur.setRole(candidateRole.get());
            user.getUserRoles().add(ur);
            userRepository.save(user);
        }

        // 5. Creer le profil candidat
        Candidate candidate = new Candidate();
        candidate.setEmail(request.getEmail());
        candidate.setFirstName(request.getFirstName());
        candidate.setLastName(request.getLastName());
        candidate.setPhone(request.getPhone());
        candidate.setStatus(CandidateStatus.ACTIVE);
        candidate.setVerified(false);
        candidateRepository.save(candidate);
        log.info("Candidate profile created: {} ({})", candidate.getEmail(), candidate.getId());

        // 6. Retourner la reponse
        return ResponseEntity.ok(RegisterResponse.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .message("Inscription reussie. Vous pouvez maintenant vous connecter.")
            .build());
    }

    private Tenant findOrCreateTenant(String tenantCode) {
        if (tenantCode != null && !tenantCode.isBlank()) {
            Optional<Tenant> existing = tenantRepository.findByCode(tenantCode);
            if (existing.isPresent()) return existing.get();
        }
        // Tenant par defaut
        Optional<Tenant> defaultTenant = tenantRepository.findByCode("default");
        if (defaultTenant.isPresent()) return defaultTenant.get();

        // Creer le tenant par defaut
        Tenant tenant = new Tenant();
        tenant.setName("Orientation Platform");
        tenant.setCode("default");
        tenant.setActive(true);
        return tenantRepository.save(tenant);
    }
}
