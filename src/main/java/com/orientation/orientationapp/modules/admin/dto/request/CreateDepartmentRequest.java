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
public class CreateDepartmentRequest {
    @NotBlank(message = "Department name is required")
    @Size(max = 200)
    private String name;

    @Size(max = 50)
    private String code;

    @Size(max = 500)
    private String description;

    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    private UUID tenantId;

    private UUID parentId;

    @Builder.Default
    private Integer sortOrder = 0;
}
