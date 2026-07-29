package com.orientation.orientationapp.modules.auth.event;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordChangedEvent {
    private UUID userId;
    private String email;
}
