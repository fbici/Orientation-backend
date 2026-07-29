package com.orientation.orientationapp.modules.auth.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginFailedEvent {
    private String email;
    private String ipAddress;
    private String reason;
}
