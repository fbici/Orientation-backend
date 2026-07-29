package com.orientation.orientationapp.modules.admin.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantResponse {
    private UUID id;
    private String name;
    private String code;
    private UUID organizationId;
    private String organizationName;
    private String description;
    private boolean active;
    private boolean suspended;
    private String timezone;
    private String language;
    private String country;
    private String currency;
    private String subscriptionPlan;
    private int maxUsers;
    private int currentUsers;
    private int maxStorageGb;
    private Instant createdAt;
    private Instant updatedAt;
}
