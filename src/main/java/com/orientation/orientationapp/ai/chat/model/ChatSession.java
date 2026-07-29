package com.orientation.orientationapp.ai.chat.model;

import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {

    private UUID sessionId;
    private UUID userId;
    private List<ChatMessage> messages;
    private Instant createdAt;
    private Instant lastActivityAt;

    public ChatSession(UUID userId) {
        this.sessionId = UUID.randomUUID();
        this.userId = userId;
        this.messages = new ArrayList<>();
        this.createdAt = Instant.now();
        this.lastActivityAt = Instant.now();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        lastActivityAt = Instant.now();
    }
}
