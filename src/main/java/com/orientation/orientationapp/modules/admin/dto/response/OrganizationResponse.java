package com.orientation.orientationapp.modules.admin.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationResponse {
    private UUID id;
    private String name;
    private String code;
    private String description;
    private String email;
    private String phone;
    private String website;
    private String logoUrl;
    private String industry;
    private String country;
    private boolean active;
    private boolean archived;
    private int tenantCount;
    private Instant createdAt;
    private Instant updatedAt;
}
