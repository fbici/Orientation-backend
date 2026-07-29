package com.orientation.orientationapp.modules.admin.service;

import com.orientation.orientationapp.modules.admin.dto.request.CreateInvitationRequest;
import com.orientation.orientationapp.modules.admin.entity.Invitation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface InvitationService {
    Invitation create(CreateInvitationRequest request, String invitedByUserId);
    Invitation getById(UUID id);
    Page<Invitation> list(UUID tenantId, Pageable pageable);
    void resend(UUID id);
    void revoke(UUID id);
    Invitation accept(String token, String userId);
    void decline(UUID id);
}
