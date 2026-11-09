package com.orientation.orientationapp.modules.admin.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.UUID;
@Getter @Setter @NoArgsConstructor
public class UpdateUserRequest {
 @NotBlank @Email private String email;
 @NotBlank @Size(max=100) private String firstName;
 @NotBlank @Size(max=100) private String lastName;
 @Size(max=20) private String phone;
 @NotNull private UUID tenantId;
}
