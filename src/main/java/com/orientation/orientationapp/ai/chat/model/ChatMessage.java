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
    private String question;
    private String intent;
    private String status;
    private java.util.UUID decisionId;
    private com.fasterxml.jackson.databind.JsonNode data;
    private java.util.List<Object> sources;

    public enum Role {
        USER, ASSISTANT, SYSTEM
    }
}
