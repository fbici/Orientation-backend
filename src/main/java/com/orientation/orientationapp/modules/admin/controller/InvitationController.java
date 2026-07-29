package com.orientation.orientationapp.modules.admin.controller;

import com.orientation.orientationapp.modules.admin.dto.request.CreateInvitationRequest;
import com.orientation.orientationapp.modules.admin.dto.response.MessageResponse;
import com.orientation.orientationapp.modules.admin.entity.Invitation;
import com.orientation.orientationapp.modules.admin.service.InvitationService;
import com.orientation.orientationapp.security.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Invitation> create(@Valid @RequestBody CreateInvitationRequest request) {
        String userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new RuntimeException("Not authenticated"));
        return ResponseEntity.ok(invitationService.create(request, userId));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Page<Invitation>> list(
            @RequestParam UUID tenantId,
            Pageable pageable) {
        return ResponseEntity.ok(invitationService.list(tenantId, pageable));
    }

    @PostMapping("/{id}/resend")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> resend(@PathVariable UUID id) {
        invitationService.resend(id);
        return ResponseEntity.ok(MessageResponse.success("Invitation resent"));
    }

    @PostMapping("/{id}/revoke")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> revoke(@PathVariable UUID id) {
        invitationService.revoke(id);
        return ResponseEntity.ok(MessageResponse.success("Invitation revoked"));
    }

    @PostMapping("/accept")
    public ResponseEntity<MessageResponse> accept(@RequestParam String token) {
        String userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new RuntimeException("Not authenticated"));
        invitationService.accept(token, userId);
        return ResponseEntity.ok(MessageResponse.success("Invitation accepted"));
    }
}
