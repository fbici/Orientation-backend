package com.orientation.orientationapp.modules.admin.controller;

import com.orientation.orientationapp.modules.admin.dto.request.CreateUserRequest;
import com.orientation.orientationapp.modules.admin.dto.response.MessageResponse;
import com.orientation.orientationapp.modules.admin.dto.response.UserResponse;
import com.orientation.orientationapp.modules.admin.service.UserAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserAdminService userAdminService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userAdminService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> update(@PathVariable UUID id, @Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userAdminService.update(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userAdminService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> list(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(userAdminService.list(tenantId, search, pageable));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deactivate(@PathVariable UUID id) {
        userAdminService.deactivate(id);
        return ResponseEntity.ok(MessageResponse.success("User deactivated"));
    }

    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> reactivate(@PathVariable UUID id) {
        userAdminService.reactivate(id);
        return ResponseEntity.ok(MessageResponse.success("User reactivated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<MessageResponse> delete(@PathVariable UUID id) {
        userAdminService.delete(id);
        return ResponseEntity.ok(MessageResponse.success("User deleted"));
    }
}
