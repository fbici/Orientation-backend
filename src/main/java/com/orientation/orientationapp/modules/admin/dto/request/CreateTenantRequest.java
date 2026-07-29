package com.orientation.orientationapp.modules.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTenantRequest {
    @NotBlank(message = "Tenant name is required")
    @Size(max = 200)
    private String name;

    @Size(max = 50)
    private String code;

    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    @Size(max = 500)
    private String description;

    @Size(max = 50)
    private String timezone;

    @Size(max = 10)
    private String language;

    @Size(max = 5)
    private String country;

    @Size(max = 3)
    private String currency;

    @Size(max = 50)
    private String subscriptionPlan;

    @Builder.Default
    private Integer maxUsers = 100;

    @Builder.Default
    private Integer maxStorageGb = 10;
}
