package com.orientation.orientationapp.modules.admin.service.impl;

import com.orientation.orientationapp.modules.admin.dto.request.CreateUserRequest;
import com.orientation.orientationapp.modules.admin.dto.request.UpdateUserRequest;
import com.orientation.orientationapp.modules.admin.dto.response.UserResponse;
import com.orientation.orientationapp.modules.admin.service.UserAdminService;
import com.orientation.orientationapp.modules.auth.entity.User;
import com.orientation.orientationapp.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository userRepository;
    private final com.orientation.orientationapp.security.service.ResourceAccess access;
    private final com.orientation.orientationapp.modules.auth.service.RefreshTokenService sessions;
    private final PasswordEncoder passwordEncoder;

    public UserResponse create(CreateUserRequest request) {
        access.require("USER_CREATE");
        access.tenant(request.getTenantId());
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        throw new RuntimeException("User creation requires tenant context");
    }

    public UserResponse update(UUID id, UpdateUserRequest request) {
        access.require("USER_UPDATE");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        access.sameTenant(user.getTenant().getId());
        access.tenant(request.getTenantId());
        if (!user.getEmail().equalsIgnoreCase(request.getEmail()))
            throw new com.orientation.orientationapp.exception.BusinessException("Email changes require verification","EMAIL_CHANGE_NOT_SUPPORTED");
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        return mapToResponse(userRepository.save(user));
    }

    public UserResponse getById(UUID id) {
        access.require("USER_READ");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        access.sameTenant(user.getTenant().getId());
        return mapToResponse(user);
    }

    public Page<UserResponse> list(UUID tenantId, String search, Pageable pageable) {
        access.require("USER_READ");
        UUID allowed=access.tenant(tenantId);
        return userRepository.findAll((root,query,cb) -> {
            var predicate=cb.equal(root.get("tenant").get("id"),allowed);
            if(search==null || search.isBlank()) return predicate;
            return cb.and(predicate,cb.or(cb.like(cb.lower(root.get("email")),"%"+search.toLowerCase()+"%"),
                cb.like(cb.lower(root.get("firstName")),"%"+search.toLowerCase()+"%"),
                cb.like(cb.lower(root.get("lastName")),"%"+search.toLowerCase()+"%")));
        },pageable).map(this::mapToResponse);
    }

    public void deactivate(UUID id) {
        access.require("USER_UPDATE");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        access.sameTenant(user.getTenant().getId());
        user.setEnabled(false);
        sessions.revokeAllUserTokens(user.getId());
        userRepository.save(user);
    }

    public void reactivate(UUID id) {
        access.require("USER_UPDATE");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        access.sameTenant(user.getTenant().getId());
        user.setEnabled(true);
        userRepository.save(user);
    }

    public void delete(UUID id) {
        access.require("USER_DELETE");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        access.sameTenant(user.getTenant().getId());
        user.setEnabled(false);
        sessions.revokeAllUserTokens(user.getId());
        userRepository.save(user);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .phone(user.getPhone())
                .tenantId(user.getTenant().getId())
                .tenantName(user.getTenant().getName())
                .status(user.getStatus().name())
                .enabled(user.getEnabled())
                .roles(user.getUserRoles().stream().map(m -> m.getRole().getCode()).distinct().toList())
                .build();
    }
}
