package com.orientation.orientationapp.security;
import com.orientation.orientationapp.backoffice.config.SocketAuthenticationInterceptor;
import com.orientation.orientationapp.security.jwt.JwtTokenProvider;
import com.orientation.orientationapp.modules.auth.service.impl.RefreshTokenServiceImpl;
import com.orientation.orientationapp.security.service.*;
import org.junit.jupiter.api.*;
import org.springframework.messaging.*;
import org.springframework.messaging.support.*;
import org.springframework.messaging.simp.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.security.access.AccessDeniedException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;

class SocketSecurityTest {
    private final UUID user=UUID.randomUUID(),family=UUID.randomUUID();
    private final String session="f1-socket";
    private final JwtTokenProvider jwt=new JwtTokenProvider("f1-unit-signing-material-01234567890123456789",900000,604800000);
    private RefreshTokenServiceImpl sessions;
    private CustomUserDetailsService users;
    private SocketAuthenticationInterceptor interceptor;
    @BeforeEach void setup() {
        sessions=mock(RefreshTokenServiceImpl.class);users=mock(CustomUserDetailsService.class);
        when(sessions.isSessionActive(family,user)).thenReturn(true);
        when(users.loadUserByUsername(user.toString())).thenReturn(CustomUserDetails.builder()
            .userId(user.toString()).email("person@example.test").enabled(true)
            .authorities(List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CANDIDAT"))).build());
        interceptor=new SocketAuthenticationInterceptor(jwt,sessions,users);
    }
    private Message<byte[]> message(StompHeaderAccessor h) {
        h.setLeaveMutable(true);return MessageBuilder.createMessage(new byte[0],h.getMessageHeaders());
    }
    private StompHeaderAccessor connect() {
        var h=StompHeaderAccessor.create(StompCommand.CONNECT);h.setSessionId(session);
        h.setNativeHeader("Authorization","Bearer "+jwt.generateAccessToken(user.toString(),null,family));
        interceptor.preSend(message(h),mock(MessageChannel.class));return h;
    }
    @Test void refusesUnauthenticatedAndRefreshConnects() {
        var h=StompHeaderAccessor.create(StompCommand.CONNECT);h.setSessionId(session);
        assertThrows(AccessDeniedException.class,()->interceptor.preSend(message(h),mock(MessageChannel.class)));
        var refresh=StompHeaderAccessor.create(StompCommand.CONNECT);refresh.setSessionId(session);
        refresh.setNativeHeader("Authorization","Bearer "+jwt.generateRefreshToken(user.toString(),null));
        assertThrows(AccessDeniedException.class,()->interceptor.preSend(message(refresh),mock(MessageChannel.class)));
    }
    @Test void authenticatesUuidPrincipalAndOnlyPersonalDestinations() {
        var connected=connect();assertEquals(user.toString(),connected.getUser().getName());
        var personal=StompHeaderAccessor.create(StompCommand.SUBSCRIBE);personal.setSessionId(session);
        personal.setUser(connected.getUser());personal.setDestination("/user/queue/notifications");
        assertNotNull(interceptor.preSend(message(personal),mock(MessageChannel.class)));
        var global=StompHeaderAccessor.create(StompCommand.SUBSCRIBE);global.setSessionId(session);
        global.setUser(connected.getUser());global.setDestination("/topic/dashboard");
        assertThrows(AccessDeniedException.class,()->interceptor.preSend(message(global),mock(MessageChannel.class)));
        var alien=StompHeaderAccessor.create(StompCommand.SUBSCRIBE);alien.setSessionId(session);
        alien.setUser(connected.getUser());alien.setDestination("/user/someone-else/queue/notifications");
        assertThrows(AccessDeniedException.class,()->interceptor.preSend(message(alien),mock(MessageChannel.class)));
    }
    @Test void revocationStopsDeliveryOnAlreadySubscribedConnections() {
        connect();
        var h=SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);h.setSessionId(session);
        Message<byte[]> outbound=MessageBuilder.createMessage(new byte[0],h.getMessageHeaders());
        assertNotNull(interceptor.outbound(outbound));
        when(sessions.isSessionActive(family,user)).thenReturn(false);
        assertNull(interceptor.outbound(outbound));
    }
    @Test void refusesInboundBusinessMessages() {
        var connected=connect();var h=StompHeaderAccessor.create(StompCommand.SEND);h.setSessionId(session);
        h.setUser(connected.getUser());h.setDestination("/app/arbitrary");
        assertThrows(AccessDeniedException.class,()->interceptor.preSend(message(h),mock(MessageChannel.class)));
    }
}
