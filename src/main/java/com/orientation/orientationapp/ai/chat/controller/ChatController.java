package com.orientation.orientationapp.ai.chat.controller;

import com.orientation.orientationapp.ai.chat.model.ChatMessage;
import com.orientation.orientationapp.ai.chat.model.ChatSession;
import com.orientation.orientationapp.ai.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/ai/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/sessions")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT')")
    public ResponseEntity<ChatSession> createSession() {
        return ResponseEntity.ok(chatService.createSession("current-user"));
    }

    @PostMapping("/sessions/{sessionId}/messages")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT')")
    public ResponseEntity<ChatMessage> sendMessage(
            @PathVariable UUID sessionId,
            @RequestBody Map<String, String> request) {

        ChatMessage response = chatService.sendMessage(sessionId, "current-user", request.get("message"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sessions/{sessionId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT')")
    public ResponseEntity<ChatSession> getSession(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(chatService.getSession(sessionId));
    }
}
