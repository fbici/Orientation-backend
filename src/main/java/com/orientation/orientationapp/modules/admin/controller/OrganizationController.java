package com.orientation.orientationapp.modules.admin.controller;

import com.orientation.orientationapp.modules.admin.dto.request.CreateOrganizationRequest;
import com.orientation.orientationapp.modules.admin.dto.response.MessageResponse;
import com.orientation.orientationapp.modules.admin.dto.response.OrganizationResponse;
import com.orientation.orientationapp.modules.admin.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<OrganizationResponse> create(@Valid @RequestBody CreateOrganizationRequest request) {
        return ResponseEntity.ok(organizationService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<OrganizationResponse> update(@PathVariable UUID id, @Valid @RequestBody CreateOrganizationRequest request) {
        return ResponseEntity.ok(organizationService.update(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<OrganizationResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(organizationService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Page<OrganizationResponse>> list(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(organizationService.list(search, pageable));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> activate(@PathVariable UUID id) {
        organizationService.activate(id);
        return ResponseEntity.ok(MessageResponse.success("Organization activated"));
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> suspend(@PathVariable UUID id) {
        organizationService.suspend(id);
        return ResponseEntity.ok(MessageResponse.success("Organization suspended"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<MessageResponse> delete(@PathVariable UUID id) {
        organizationService.delete(id);
        return ResponseEntity.ok(MessageResponse.success("Organization deleted"));
    }
}
