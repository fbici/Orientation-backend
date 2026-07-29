package com.orientation.orientationapp.ai.chat.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    private UUID id;
    private String role;
    private String content;
    private Instant timestamp;
    private String sessionId;

    public enum Role {
        USER, ASSISTANT, SYSTEM
    }
}
