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
    private final com.orientation.orientationapp.security.service.ResourceAccess access;
    private final TenantRepository tenantRepository;
    private final UserAdminRepository userAdminRepository;

    @Override
    @Transactional
    public Invitation create(CreateInvitationRequest request, String invitedByUserId) {
        access.require("USER_CREATE");
        access.tenant(request.getTenantId());
        access.personalUser(invitedByUserId);
        access.require("USER_CREATE");
        access.tenant(request.getTenantId());
        access.personalUser(invitedByUserId);
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
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Invitation getById(UUID id) {
        var invitation=invitationRepository.findById(id)
                .orElseThrow(com.orientation.orientationapp.security.service.ResourceAccess::denied);
        access.sameTenant(invitation.getTenant().getId());access.require("USER_READ");return invitation;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Invitation> list(UUID tenantId, Pageable pageable) {
        access.require("USER_READ");
        return invitationRepository.findByTenantId(access.tenant(tenantId), pageable);
    }

    @Override
    @Transactional
    public void resend(UUID id) {
        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invitation not found: " + id));

        access.require("USER_UPDATE");
        access.sameTenant(invitation.getTenant().getId());
        access.require("USER_UPDATE");
        access.sameTenant(invitation.getTenant().getId());
        if (invitation.getStatus() == Invitation.InvitationStatus.ACCEPTED) {
            throw new RuntimeException("Cannot resend accepted invitation");
        }

        invitation.setToken(UUID.randomUUID().toString());
        invitation.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        invitation.setStatus(Invitation.InvitationStatus.PENDING);
        invitation.setRevokedAt(null);
        invitationRepository.save(invitation);
    }

    @Override
    @Transactional
    public void revoke(UUID id) {
        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invitation not found: " + id));
        access.require("USER_UPDATE");
        access.sameTenant(invitation.getTenant().getId());
        access.require("USER_UPDATE");
        access.sameTenant(invitation.getTenant().getId());
        invitation.setStatus(Invitation.InvitationStatus.REVOKED);
        invitation.setRevokedAt(Instant.now());
        invitationRepository.save(invitation);
    }

    @Override
    @Transactional
    public Invitation accept(String token, String userId) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid invitation token"));

        var current=access.current();
        access.personalUser(userId);
        if (!invitation.getEmail().equalsIgnoreCase(current.getEmail()))
            throw com.orientation.orientationapp.security.service.ResourceAccess.denied();
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
        return invitation;
    }

    @Override
    @Transactional
    public void decline(UUID id) {
        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invitation not found: " + id));
        access.require("USER_UPDATE");
        access.sameTenant(invitation.getTenant().getId());
        access.require("USER_UPDATE");
        access.sameTenant(invitation.getTenant().getId());
        invitation.setStatus(Invitation.InvitationStatus.DECLINED);
        invitationRepository.save(invitation);
    }
}
