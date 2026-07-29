package com.orientation.orientationapp.modules.admin.controller;

import com.orientation.orientationapp.modules.admin.dto.request.CreateTenantRequest;
import com.orientation.orientationapp.modules.admin.dto.response.MessageResponse;
import com.orientation.orientationapp.modules.admin.dto.response.TenantResponse;
import com.orientation.orientationapp.modules.admin.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<TenantResponse> create(@Valid @RequestBody CreateTenantRequest request) {
        return ResponseEntity.ok(tenantService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<TenantResponse> update(@PathVariable UUID id, @Valid @RequestBody CreateTenantRequest request) {
        return ResponseEntity.ok(tenantService.update(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<TenantResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Page<TenantResponse>> list(
            @RequestParam(required = false) UUID organizationId,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(tenantService.list(organizationId, search, pageable));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> activate(@PathVariable UUID id) {
        tenantService.activate(id);
        return ResponseEntity.ok(MessageResponse.success("Tenant activated"));
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> suspend(@PathVariable UUID id) {
        tenantService.suspend(id);
        return ResponseEntity.ok(MessageResponse.success("Tenant suspended"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<MessageResponse> delete(@PathVariable UUID id) {
        tenantService.delete(id);
        return ResponseEntity.ok(MessageResponse.success("Tenant deleted"));
    }
}
