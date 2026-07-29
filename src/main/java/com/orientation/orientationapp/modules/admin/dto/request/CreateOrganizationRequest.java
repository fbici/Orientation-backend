package com.orientation.orientationapp.modules.admin.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrganizationRequest {
    @NotBlank(message = "Organization name is required")
    @Size(max = 200)
    private String name;

    @Size(max = 50)
    private String code;

    @Size(max = 500)
    private String description;

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 500)
    private String website;

    @Size(max = 100)
    private String industry;

    @Size(max = 100)
    private String country;
}
