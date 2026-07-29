package com.orientation.orientationapp.modules.admin.service;

import com.orientation.orientationapp.modules.admin.dto.request.CreateUserRequest;
import com.orientation.orientationapp.modules.admin.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserAdminService {
    UserResponse create(CreateUserRequest request);
    UserResponse update(UUID id, CreateUserRequest request);
    UserResponse getById(UUID id);
    Page<UserResponse> list(UUID tenantId, String search, Pageable pageable);
    void deactivate(UUID id);
    void reactivate(UUID id);
    void delete(UUID id);
}
