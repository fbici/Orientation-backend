package com.orientation.orientationapp.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        log.error("Unauthorized access attempt: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        // Extraire le message d'erreur original
        String errorMessage = "Authentication required. Please provide a valid JWT token.";
        Throwable cause = authException.getCause();
        if (cause != null && cause.getMessage() != null) {
            errorMessage = cause.getMessage();
        } else if (authException.getMessage() != null) {
            errorMessage = authException.getMessage();
        }
        
        // Determiner le status code
        int status = HttpServletResponse.SC_UNAUTHORIZED;
        if ("EMAIL_NOT_VERIFIED".equals(errorMessage)) {
            status = HttpServletResponse.SC_FORBIDDEN;
        }
        
        response.setStatus(status);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", status);
        errorResponse.put("error", status == 403 ? "Forbidden" : "Unauthorized");
        errorResponse.put("message", errorMessage);
        errorResponse.put("path", request.getServletPath());

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
