package com.orientation.orientationapp.modules.admin.service.impl;

import com.orientation.orientationapp.modules.admin.dto.request.CreateInvitationRequest;
import com.orientation.orientationapp.modules.admin.entity.Invitation;
import com.orientation.orientationapp.modules.auth.entity.Tenant;
import com.orientation.orientationapp.modules.auth.entity.User;
import com.orientation.orientationapp.modules.admin.repository.InvitationRepository;
import com.orientation.orientationapp.modules.auth.repository.TenantRepository;
import com.orientation.orientationapp.modules.admin.repository.UserAdminRepository;
import com.orientation.orientationapp.modules.admin.service.InvitationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {

    private final InvitationRepository invitationRepository;
    private final TenantRepository tenantRepository;
    private final UserAdminRepository userAdminRepository;

    @Override
    @Transactional
    public Invitation create(CreateInvitationRequest request, String invitedByUserId) {
        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + request.getTenantId()));

        User inviter = userAdminRepository.findById(UUID.fromString(invitedByUserId))
                .orElseThrow(() -> new RuntimeException("Inviter not found: " + invitedByUserId));

        if (invitationRepository.existsByEmailAndTenantIdAndStatus(request.getEmail(), request.getTenantId(), Invitation.InvitationStatus.PENDING)) {
            throw new RuntimeException("Pending invitation already exists for this email");
        }

        Invitation invitation = Invitation.builder()
                .email(request.getEmail())
                .tenant(tenant)
                .invitedBy(inviter)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .message(request.getMessage())
                .build();

        Invitation saved = invitationRepository.save(invitation);
        log.info("Invitation created for {} by {}", request.getEmail(), inviter.getEmail());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Invitation getById(UUID id) {
        return invitationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invitation not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Invitation> list(UUID tenantId, Pageable pageable) {
        return invitationRepository.findByTenantId(tenantId, pageable);
    }

    @Override
    @Transactional
    public void resend(UUID id) {
        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invitation not found: " + id));

        if (invitation.getStatus() == Invitation.InvitationStatus.ACCEPTED) {
            throw new RuntimeException("Cannot resend accepted invitation");
        }

        invitation.setToken(UUID.randomUUID().toString());
        invitation.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        invitation.setStatus(Invitation.InvitationStatus.PENDING);
        invitation.setRevokedAt(null);
        invitationRepository.save(invitation);
        log.info("Invitation resent for {}", invitation.getEmail());
    }

    @Override
    @Transactional
    public void revoke(UUID id) {
        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invitation not found: " + id));
        invitation.setStatus(Invitation.InvitationStatus.REVOKED);
        invitation.setRevokedAt(Instant.now());
        invitationRepository.save(invitation);
    }

    @Override
    @Transactional
    public Invitation accept(String token, String userId) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid invitation token"));

        if (invitation.getStatus() != Invitation.InvitationStatus.PENDING) {
            throw new RuntimeException("Invitation is not pending");
        }

        if (invitation.getExpiresAt().isBefore(Instant.now())) {
            invitation.setStatus(Invitation.InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new RuntimeException("Invitation has expired");
        }

        invitation.setStatus(Invitation.InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(Instant.now());
        invitationRepository.save(invitation);
        log.info("Invitation accepted by user {}", userId);
        return invitation;
    }

    @Override
    @Transactional
    public void decline(UUID id) {
        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invitation not found: " + id));
        invitation.setStatus(Invitation.InvitationStatus.DECLINED);
        invitationRepository.save(invitation);
    }
}
