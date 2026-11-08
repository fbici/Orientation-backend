package com.orientation.orientationapp.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest {
    @NotBlank(message = "Token is required")
    private String token;

    @NotBlank(message = "New password is required")
    @Size(min = 12, max = 72, message = "Password must be at least 8 characters")
    @com.fasterxml.jackson.annotation.JsonAlias("password")
    private String newPassword;
}
