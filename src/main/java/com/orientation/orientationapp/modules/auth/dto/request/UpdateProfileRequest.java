package com.orientation.orientationapp.modules.auth.dto.request;
import lombok.Data;
import jakarta.validation.constraints.*;
@Data
public class UpdateProfileRequest {
    @NotBlank @Size(max=100) private String firstName;
    @NotBlank @Size(max=100) private String lastName;
    @Size(max=20) private String phone;
}
