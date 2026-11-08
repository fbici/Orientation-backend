package com.orientation.orientationapp.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequest {
    @NotBlank(message = "Current password is required")
    @com.fasterxml.jackson.annotation.JsonAlias("oldPassword")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 12, max = 72, message = "Password must be at least 8 characters")
    private String newPassword;
}
