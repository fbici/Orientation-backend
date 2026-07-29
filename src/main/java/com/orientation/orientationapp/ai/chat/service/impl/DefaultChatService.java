package com.orientation.orientationapp.ai.chat.service.impl;

import com.orientation.orientationapp.ai.chat.model.ChatMessage;
import com.orientation.orientationapp.ai.chat.model.ChatSession;
import com.orientation.orientationapp.ai.chat.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class DefaultChatService implements ChatService {

    private final Map<UUID, ChatSession> sessions = new ConcurrentHashMap<>();

    private static final Map<String, String> FAQ_RESPONSES = Map.of(
            "université", "Il existe plusieurs universités au Maroc. Les plus recommandées sont l'Université Mohammed V, Hassan II et Cadi Ayyad. Voulez-vous des détails sur une université spécifique ?",
            "programme", "Nous avons plusieurs programmes disponibles : Informatique, Médecine, Droit, Gestion, etc. Quel domaine vous intéresse ?",
            "bourse", "Plusieurs bourses sont disponibles : Bourse au Mérite, Bourse Gouvernementale, Bourse d'Excellence. Voulez-vous en savoir plus ?",
            "admission", "Les critères d'admission varient selon les programmes. En général, une moyenne de 12/20 est requise. Quel programme vous intéresse ?",
            "comparer", "Je peux vous aider à comparer des universités ou des programmes. Quels éléments souhaitez-vous comparer ?",
            "chances", "Vos chances dépendent de votre moyenne, vos notes par matière et le programme choisi. Voulez-vous que j'analyse votre profil ?",
            "améliorer", "Pour améliorer vos chances : concentrez-vous sur les matières clés du programme, visez une moyenne supérieure au minimum, et préparez bien votre dossier."
    );

    @Override
    public ChatMessage sendMessage(UUID sessionId, String userId, String message) {
        log.info("Chat message from user {} in session {}", userId, sessionId);

        ChatSession session = sessions.computeIfAbsent(sessionId, id -> new ChatSession(UUID.fromString(userId)));

        ChatMessage userMessage = ChatMessage.builder()
                .id(UUID.randomUUID())
                .role(ChatMessage.Role.USER.name())
                .content(message)
                .timestamp(Instant.now())
                .sessionId(sessionId.toString())
                .build();

        session.addMessage(userMessage);

        // Generate response
        String response = generateResponse(message.toLowerCase(), session);
        ChatMessage assistantMessage = ChatMessage.builder()
                .id(UUID.randomUUID())
                .role(ChatMessage.Role.ASSISTANT.name())
                .content(response)
                .timestamp(Instant.now())
                .sessionId(sessionId.toString())
                .build();

        session.addMessage(assistantMessage);

        return assistantMessage;
    }

    @Override
    public ChatSession createSession(String userId) {
        ChatSession session = new ChatSession(UUID.fromString(userId));
        sessions.put(session.getSessionId(), session);

        ChatMessage welcomeMessage = ChatMessage.builder()
                .id(UUID.randomUUID())
                .role(ChatMessage.Role.ASSISTANT.name())
                .content("Bonjour ! Je suis votre assistant d'orientation. Comment puis-je vous aider ?")
                .timestamp(Instant.now())
                .sessionId(session.getSessionId().toString())
                .build();

        session.addMessage(welcomeMessage);
        return session;
    }

    @Override
    public ChatSession getSession(UUID sessionId) {
        return sessions.get(sessionId);
    }

    private String generateResponse(String message, ChatSession session) {
        // Simple keyword matching for FAQ
        for (Map.Entry<String, String> entry : FAQ_RESPONSES.entrySet()) {
            if (message.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Default response
        return "Merci pour votre question. Je peux vous aider avec :\n" +
               "• Les universités et programmes\n" +
               "• Les critères d'admission\n" +
               "• Les bourses disponibles\n" +
               "• La comparaison d'options\n" +
               "• L'amélioration de vos chances\n" +
               "\nQue souhaitez-vous savoir ?";
    }
}
