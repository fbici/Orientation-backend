package com.orientation.orientationapp.modules.auth.controller;

import com.orientation.orientationapp.modules.auth.dto.request.*;
import com.orientation.orientationapp.modules.auth.dto.response.LoginResponse;
import com.orientation.orientationapp.modules.auth.dto.response.MessageResponse;
import com.orientation.orientationapp.modules.auth.dto.response.UserInfo;
import com.orientation.orientationapp.modules.auth.service.AuthenticationService;
import com.orientation.orientationapp.security.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse response = authenticationService.login(request, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        LoginResponse response = authenticationService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @RequestBody(required = false) RefreshTokenRequest request) {

        String userId = SecurityUtils.getCurrentUserId().orElse(null);
        String refreshToken = request != null ? request.getRefreshToken() : null;

        MessageResponse response = authenticationService.logout(refreshToken, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        MessageResponse response = authenticationService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        MessageResponse response = authenticationService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfo> getCurrentUser() {
        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("Not authenticated"));

        UserInfo userInfo = authenticationService.getCurrentUser(userId);
        return ResponseEntity.ok(userInfo);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
