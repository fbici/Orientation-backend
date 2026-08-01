package com.orientation.orientationapp.modules.auth.controller;

import com.orientation.orientationapp.common.enums.CandidateStatus;
import com.orientation.orientationapp.modules.auth.dto.request.RegisterRequest;
import com.orientation.orientationapp.modules.auth.dto.response.RegisterResponse;
import com.orientation.orientationapp.modules.auth.entity.Role;
import com.orientation.orientationapp.modules.auth.entity.Tenant;
import com.orientation.orientationapp.modules.auth.entity.User;
import com.orientation.orientationapp.modules.auth.repository.RoleRepository;
import com.orientation.orientationapp.modules.auth.repository.TenantRepository;
import com.orientation.orientationapp.modules.auth.repository.UserRepository;
import com.orientation.orientationapp.modules.auth.service.EmailService;
import com.orientation.orientationapp.modules.user.entity.Candidate;
import com.orientation.orientationapp.modules.user.repository.CandidateRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Inscription, connexion, gestion des tokens")
public class RegisterController {

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Operation(summary = "Inscription d'un candidat")
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());

        // 1. Vérifier si l'email existe déjà
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(
                RegisterResponse.builder().message("Cet email est deja utilise").build()
            );
        }

        // 2. Trouver ou créer le tenant
        Tenant tenant = findOrCreateTenant(request.getTenantCode());

        // 3. Générer le token de vérification
        String verificationToken = UUID.randomUUID().toString();

        // 4. Créer l'utilisateur
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

        // 5. Assigner le rôle CANDIDAT
        Optional<Role> candidateRole = roleRepository.findByCode("CANDIDAT");
        if (candidateRole.isPresent()) {
            com.orientation.orientationapp.modules.auth.entity.UserRole ur = new com.orientation.orientationapp.modules.auth.entity.UserRole();
            ur.setUser(user);
            ur.setRole(candidateRole.get());
            user.getUserRoles().add(ur);
            userRepository.save(user);
        }

        // 6. Créer le profil candidat
        Candidate candidate = new Candidate();
        candidate.setEmail(request.getEmail());
        candidate.setFirstName(request.getFirstName());
        candidate.setLastName(request.getLastName());
        candidate.setPhone(request.getPhone());
        candidate.setStatus(CandidateStatus.ACTIVE);
        candidate.setVerified(false);
        candidateRepository.save(candidate);

        // 7. Envoyer l'email de vérification
        try {
            emailService.sendVerificationEmail(
                request.getEmail(),
                request.getFirstName(),
                verificationToken
            );
        } catch (Exception e) {
            log.warn("Failed to send verification email: {}", e.getMessage());
        }

        // 8. Retourner la réponse
        return ResponseEntity.ok(RegisterResponse.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .message("Inscription reussie. Verifiez votre email pour activer votre compte.")
            .build());
    }

    @Operation(summary = "Vérifier l'email avec le token")
    @GetMapping("/verify")
    public ResponseEntity<RegisterResponse> verifyEmail(@RequestParam String token) {
        // En production, valider le token et marquer emailVerified = true
        log.info("Email verification attempt with token: {}", token);
        return ResponseEntity.ok(RegisterResponse.builder()
            .message("Email verifie avec succes. Vous pouvez maintenant vous connecter.")
            .build());
    }

    private Tenant findOrCreateTenant(String tenantCode) {
        if (tenantCode != null && !tenantCode.isBlank()) {
            Optional<Tenant> existing = tenantRepository.findByCode(tenantCode);
            if (existing.isPresent()) return existing.get();
        }
        Optional<Tenant> defaultTenant = tenantRepository.findByCode("default");
        if (defaultTenant.isPresent()) return defaultTenant.get();

        Tenant tenant = new Tenant();
        tenant.setName("Orientation Platform");
        tenant.setCode("default");
        tenant.setActive(true);
        return tenantRepository.save(tenant);
    }
}
