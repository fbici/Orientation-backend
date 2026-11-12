package com.orientation.orientationapp.modules.auth.dto.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfo {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS)
    private String phone;
    private List<String> roles;
    private List<String> permissions;
    private UUID tenantId;
    private String tenantName;
}
