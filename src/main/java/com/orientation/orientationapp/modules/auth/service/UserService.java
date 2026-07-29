package com.orientation.orientationapp.modules.auth.service;

import com.orientation.orientationapp.modules.auth.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User findById(UUID id);
    User findByEmail(String email);
    List<User> findByTenantId(UUID tenantId);
    User create(User user);
    User update(User user);
    void changePassword(UUID userId, String currentPassword, String newPassword);
    void unlockAccount(UUID userId);
    boolean existsByEmail(String email);
}
