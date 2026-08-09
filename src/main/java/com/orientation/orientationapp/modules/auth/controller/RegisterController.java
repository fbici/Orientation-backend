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
import com.orientation.orientationapp.modules.university.entity.Country;
import com.orientation.orientationapp.modules.university.entity.City;
import io.swagger.v3.oas.annotations.Operation;
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

        try {
            // 2. Trouver ou créer le tenant
            Tenant tenant = findOrCreateTenant(request.getTenantCode());

            // 3. Créer l'utilisateur
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
            user.setDeleted(false);

            user = userRepository.save(user);
            log.info("User created: {} ({})", user.getEmail(), user.getId());

            // 4. Assigner le rôle CANDIDAT
            Optional<Role> candidateRole = roleRepository.findByCode("CANDIDAT");
            if (candidateRole.isPresent()) {
                var ur = new com.orientation.orientationapp.modules.auth.entity.UserRole();
                ur.setUser(user);
                ur.setRole(candidateRole.get());
                user.getUserRoles().add(ur);
                userRepository.save(user);
                log.info("Role CANDIDAT assigned to {}", user.getEmail());
            }

            // 5. Créer le profil candidat
            try {
                Candidate candidate = new Candidate();
                candidate.setEmail(request.getEmail());
                candidate.setFirstName(request.getFirstName());
                candidate.setLastName(request.getLastName());
                candidate.setPhone(request.getPhone());
                candidate.setStatus(CandidateStatus.ACTIVE);
                candidate.setVerified(false);
                candidate.setDeleted(false);
                // Pays et ville si fournis
                if (request.getCountryId() != null && !request.getCountryId().isBlank()) {
                    try {
                        java.util.UUID countryUuid = java.util.UUID.fromString(request.getCountryId());
                        Country country = new Country();
                        country.setId(countryUuid);
                        candidate.setCountry(country);
                    } catch (Exception ignored) {}
                }
                if (request.getCityId() != null && !request.getCityId().isBlank()) {
                    try {
                        java.util.UUID cityUuid = java.util.UUID.fromString(request.getCityId());
                        City city = new City();
                        city.setId(cityUuid);
                        candidate.setCity(city);
                    } catch (Exception ignored) {}
                }
                candidateRepository.save(candidate);
                log.info("Candidate profile created: {}", candidate.getEmail());
            } catch (Exception e) {
                log.warn("Failed to create candidate profile: {}", e.getMessage());
            }

            // 6. Envoyer l'email de verification (non-bloquant)
            String verificationToken = UUID.randomUUID().toString();
            try {
                // Stocker le token dans un champ temporaire ou une table dediee
                user.setMfaSecret(verificationToken); // Reutilisation temporaire
                userRepository.save(user);
                emailService.sendVerificationEmail(
                    request.getEmail(),
                    request.getFirstName(),
                    verificationToken
                );
            } catch (Exception e) {
                log.warn("Failed to send verification email: {}", e.getMessage());
            }

            // 7. Retourner la réponse
            return ResponseEntity.ok(RegisterResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .message("Inscription reussie. Verifiez votre email pour activer votre compte.")
                .build());

        } catch (Exception e) {
            log.error("Registration failed for {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                RegisterResponse.builder().message("Erreur lors de l'inscription: " + e.getMessage()).build()
            );
        }
    }

    @Operation(summary = "Vérifier l'email avec le token")
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        log.info("Email verification attempt with token: {}", token);
        
        // Chercher l'utilisateur avec ce token
        Optional<User> userOpt = userRepository.findAll().stream()
            .filter(u -> token.equals(u.getMfaSecret()))
            .findFirst();
        
        String html;
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEmailVerified(true);
            user.setMfaSecret(null);
            userRepository.save(user);
            
            // Aussi mettre a jour le candidat
            candidateRepository.findByEmail(user.getEmail()).ifPresent(c -> {
                c.setVerified(true);
                candidateRepository.save(c);
            });
            
            log.info("Email verified for: {}", user.getEmail());
            html = successHtml(user.getFirstName(), user.getEmail());
        } else {
            html = errorHtml();
        }
        
        return ResponseEntity.ok()
            .header("Content-Type", "text/html; charset=UTF-8")
            .body(html);
    }

    private String successHtml(String firstName, String email) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Email verifie - Orientia</title>
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body { font-family: 'Inter', -apple-system, sans-serif; background: #f3f4f6; min-height: 100vh; display: flex; align-items: center; justify-content: center; }
                .card { background: white; border-radius: 20px; padding: 48px; max-width: 440px; width: 90%; text-align: center; box-shadow: 0 4px 24px rgba(0,0,0,0.08); }
                .icon { width: 72px; height: 72px; background: #dcfce7; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 24px; }
                .icon svg { width: 36px; height: 36px; color: #22c55e; }
                h1 { font-size: 24px; font-weight: 800; color: #111827; margin-bottom: 8px; }
                p { color: #6b7280; font-size: 15px; line-height: 1.6; margin-bottom: 8px; }
                .email { font-weight: 700; color: #2563eb; }
                .btn { display: inline-block; background: #2563eb; color: white; padding: 14px 32px; border-radius: 12px; font-weight: 700; font-size: 15px; text-decoration: none; margin-top: 24px; }
                .footer { margin-top: 32px; color: #9ca3af; font-size: 12px; }
            </style>
            </head>
            <body>
                <div class="card">
                    <div class="icon">
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                    </div>
                    <h1>Email verifie !</h1>
                    <p>Bienvenue <strong>%s</strong>,</p>
                    <p>Votre adresse <span class="email">%s</span> a ete confirmee avec succes.</p>
                    <p>Vous pouvez maintenant vous connecter a l'application Orientia.</p>
                    <a href="https://orientation-backend-4lcp.onrender.com" class="btn">Ouvrir Orientia</a>
                    <p class="footer">© Orientia — Plateforme d'orientation universitaire</p>
                </div>
            </body>
            </html>
            """.formatted(firstName != null ? firstName : "Utilisateur", email != null ? email : "");
    }

    private String errorHtml() {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Lien invalide - Orientia</title>
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body { font-family: 'Inter', -apple-system, sans-serif; background: #f3f4f6; min-height: 100vh; display: flex; align-items: center; justify-content: center; }
                .card { background: white; border-radius: 20px; padding: 48px; max-width: 440px; width: 90%; text-align: center; box-shadow: 0 4px 24px rgba(0,0,0,0.08); }
                .icon { width: 72px; height: 72px; background: #fef2f2; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 24px; }
                .icon svg { width: 36px; height: 36px; color: #ef4444; }
                h1 { font-size: 24px; font-weight: 800; color: #111827; margin-bottom: 8px; }
                p { color: #6b7280; font-size: 15px; line-height: 1.6; margin-bottom: 8px; }
                .footer { margin-top: 32px; color: #9ca3af; font-size: 12px; }
            </style>
            </head>
            <body>
                <div class="card">
                    <div class="icon">
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z" /></svg>
                    </div>
                    <h1>Lien invalide</h1>
                    <p>Ce lien de verification est invalide ou a expire.</p>
                    <p>Veuillez vous inscrire a nouveau ou contacter le support.</p>
                    <p class="footer">© Orientia — Plateforme d'orientation universitaire</p>
                </div>
            </body>
            </html>
            """;
    }

    private Tenant findOrCreateTenant(String tenantCode) {
        if (tenantCode != null && !tenantCode.isBlank()) {
            Optional<Tenant> existing = tenantRepository.findByCode(tenantCode);
            if (existing.isPresent()) return existing.get();
        }
        Optional<Tenant> defaultTenant = tenantRepository.findByCode("default");
        if (defaultTenant.isPresent()) return defaultTenant.get();

        Tenant tenant = new Tenant();
        tenant.setName("Orientia Platform");
        tenant.setCode("default");
        tenant.setActive(true);
        return tenantRepository.save(tenant);
    }
}
