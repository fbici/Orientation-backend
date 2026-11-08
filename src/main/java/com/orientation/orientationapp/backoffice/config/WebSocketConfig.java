package com.orientation.orientationapp.backoffice.config;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.config.*;
import org.springframework.web.socket.config.annotation.*;
@Configuration @EnableWebSocketMessageBroker @RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final SocketAuthenticationInterceptor authorization;
    @Value("${app.cors.allowed-origins}") private String origins;
    @Override public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic","/queue");
        config.setApplicationDestinationPrefixes("/app");
    }
    @Override public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authorization);
    }
    @Override public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(new org.springframework.messaging.support.ChannelInterceptor() {
            @Override public org.springframework.messaging.Message<?> preSend(
                    org.springframework.messaging.Message<?> message,org.springframework.messaging.MessageChannel channel) {
                return authorization.outbound(message);
            }
        });
    }
    @Override public void registerStompEndpoints(StompEndpointRegistry registry) {
        if(origins.contains("*")) throw new IllegalStateException("Explicit socket origins are required");
        registry.addEndpoint("/ws").setAllowedOrigins(origins.split(",")).withSockJS();
    }
}
