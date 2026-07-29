package com.orientation.orientationapp.modules.auth.service;

import com.orientation.orientationapp.modules.auth.entity.Role;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    Role findById(UUID id);
    Role findByCode(String code);
    List<Role> findAll();
    Role create(Role role);
    void assignRoleToUser(UUID userId, UUID roleId);
    void removeRoleFromUser(UUID userId, UUID roleId);
}
