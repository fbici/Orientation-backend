package com.orientation.orientationapp.backoffice.config;
import com.orientation.orientationapp.security.jwt.JwtTokenProvider;
import com.orientation.orientationapp.modules.auth.service.impl.RefreshTokenServiceImpl;
import com.orientation.orientationapp.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.messaging.*;
import org.springframework.messaging.support.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import java.util.*;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
@Component @RequiredArgsConstructor
public class SocketAuthenticationInterceptor implements ChannelInterceptor {
    private final JwtTokenProvider jwt;
    private final RefreshTokenServiceImpl sessions;
    private final CustomUserDetailsService users;
    private record Connection(UUID family,UUID user,Instant expiration) {}
    private final Map<String,Connection> connections=new ConcurrentHashMap<>();
    private boolean has(Authentication a,String code) {
        return a.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals(code));
    }
    private boolean live(Connection c) {
        return c!=null && c.expiration().isAfter(Instant.now())
            && sessions.isSessionActive(c.family(),c.user())
            && users.loadUserByUsername(c.user().toString()).isEnabled();
    }
    @Override public Message<?> preSend(Message<?> message,MessageChannel channel) {
        var headers=MessageHeaderAccessor.getAccessor(message,StompHeaderAccessor.class);
        if(headers==null || headers.getCommand()==null) return message;
        String sessionId=headers.getSessionId();
        if(headers.getCommand()==StompCommand.CONNECT) {
            String bearer=headers.getFirstNativeHeader("Authorization");
            try {
                if(bearer==null || !bearer.startsWith("Bearer ") || sessionId==null) throw new IllegalArgumentException();
                var claims=jwt.getAccessClaims(bearer.substring(7));
                var user=users.loadUserByUsername(claims.getSubject());
                var connection=new Connection(UUID.fromString(claims.get("sid",String.class)),
                    UUID.fromString(claims.getSubject()),claims.getExpiration().toInstant());
                if(!user.isEnabled() || !live(connection) || connections.size()>=10000) throw new IllegalArgumentException();
                // Broker user destinations use the immutable account UUID, not the email address.
                headers.setUser(new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities()) {
                    @Override public String getName() { return connection.user().toString(); }
                });
                connections.put(sessionId,connection);
            } catch(RuntimeException ex) { throw new AccessDeniedException("Socket authentication required"); }
            return message;
        }
        if(headers.getCommand()==StompCommand.DISCONNECT) { connections.remove(sessionId);return message; }
        if(!(headers.getUser() instanceof Authentication auth) || !auth.isAuthenticated() || !live(connections.get(sessionId)))
            throw new AccessDeniedException("Socket session expired");
        if(headers.getCommand()==StompCommand.SUBSCRIBE) {
            String destination=headers.getDestination();
            boolean personal="/user/queue/notifications".equals(destination) || "/user/queue/pipeline".equals(destination);
            boolean global=has(auth,"ROLE_SUPER_ADMIN") && has(auth,"TENANT_ACCESS_ALL") && has(auth,"STATISTICS_READ")
                    && java.util.Set.of("/topic/dashboard","/topic/monitoring").contains(destination);
            if(!personal && !global) throw new AccessDeniedException("Socket destination not permitted");
        }
        if(headers.getCommand()==StompCommand.SEND) throw new AccessDeniedException("Socket destination not permitted");
        return message;
    }
    public Message<?> outbound(Message<?> message) {
        String id=SimpMessageHeaderAccessor.getSessionId(message.getHeaders());
        if(id==null) return message; // Broker infrastructure messages have no client recipient.
        if(SimpMessageHeaderAccessor.getMessageType(message.getHeaders())!=org.springframework.messaging.simp.SimpMessageType.MESSAGE)
            return message;
        try { return live(connections.get(id)) ? message : null; }
        catch(RuntimeException ex) { return null; } // Fail closed on revoked, expired or unavailable credentials.
    }
    @org.springframework.context.event.EventListener
    public void disconnected(org.springframework.web.socket.messaging.SessionDisconnectEvent event) {
        connections.remove(event.getSessionId());
    }
}
