package com.orientation.orientationapp.modules.admin.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private UUID tenantId;
    private String tenantName;
    private String status;
    private boolean emailVerified;
    private boolean enabled;
    private boolean mfaEnabled;
    private Instant lastLoginAt;
    private Instant createdAt;
    private List<String> roles;
}
