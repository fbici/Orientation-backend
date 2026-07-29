package com.orientation.orientationapp.ai.chat.service;

import com.orientation.orientationapp.ai.chat.model.ChatMessage;
import com.orientation.orientationapp.ai.chat.model.ChatSession;

import java.util.UUID;

public interface ChatService {

    /**
     * Send a message to the chat assistant.
     *
     * @param sessionId the session ID
     * @param userId    the user ID
     * @param message   the user message
     * @return the assistant response
     */
    ChatMessage sendMessage(UUID sessionId, String userId, String message);

    /**
     * Create a new chat session.
     *
     * @param userId the user ID
     * @return the new session
     */
    ChatSession createSession(String userId);

    /**
     * Get chat history for a session.
     *
     * @param sessionId the session ID
     * @return the chat session with history
     */
    ChatSession getSession(UUID sessionId);
}
