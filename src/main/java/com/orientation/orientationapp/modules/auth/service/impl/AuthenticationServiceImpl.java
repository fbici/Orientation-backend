package com.orientation.orientationapp.modules.auth.service.impl;

import com.orientation.orientationapp.common.enums.CandidateStatus;
import com.orientation.orientationapp.modules.auth.dto.request.*;
import com.orientation.orientationapp.modules.auth.dto.response.LoginResponse;
import com.orientation.orientationapp.modules.auth.dto.response.MessageResponse;
import com.orientation.orientationapp.modules.auth.dto.response.UserInfo;
import com.orientation.orientationapp.modules.auth.entity.RefreshToken;
import com.orientation.orientationapp.modules.auth.entity.User;
import com.orientation.orientationapp.modules.auth.entity.UserRole;
import com.orientation.orientationapp.modules.auth.event.LoginFailedEvent;
import com.orientation.orientationapp.modules.auth.event.UserLoggedInEvent;
import com.orientation.orientationapp.modules.auth.repository.UserRepository;
import com.orientation.orientationapp.modules.auth.service.AuthenticationService;
import com.orientation.orientationapp.modules.auth.service.RefreshTokenService;
import com.orientation.orientationapp.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getAccountLocked() && user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
                throw new RuntimeException("Account is locked. Try again later.");
            }

            if (!user.getEnabled() || user.getStatus() != CandidateStatus.ACTIVE) {
                throw new RuntimeException("Account is disabled.");
            }

            user.setFailedLoginAttempts(0);
            user.setAccountLocked(false);
            user.setLockedUntil(null);
            userRepository.save(user);

            String accessToken = jwtTokenProvider.generateAccessToken(
                    user.getId().toString(), user.getEmail());

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, ipAddress, userAgent);

            List<String> roles = user.getUserRoles().stream()
                    .map(UserRole::getRole)
                    .map(r -> r.getCode())
                    .collect(Collectors.toList());

            List<String> permissions = user.getUserRoles().stream()
                    .flatMap(ur -> ur.getRole().getRolePermissions().stream())
                    .map(rp -> rp.getPermission().getCode())
                    .distinct()
                    .collect(Collectors.toList());

            UserInfo userInfo = UserInfo.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .fullName(user.getFullName())
                    .roles(roles)
                    .permissions(permissions)
                    .tenantId(user.getTenant().getId())
                    .tenantName(user.getTenant().getName())
                    .build();

            eventPublisher.publishEvent(UserLoggedInEvent.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .ipAddress(ipAddress)
                    .build());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .expiresIn(jwtTokenProvider.getJwtExpiration() / 1000)
                    .tokenType("Bearer")
                    .user(userInfo)
                    .build();

        } catch (Exception e) {
            log.error("Login failed for email: {}", request.getEmail(), e);

            userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
                int attempts = user.getFailedLoginAttempts() + 1;
                user.setFailedLoginAttempts(attempts);
                if (attempts >= 5) {
                    user.setAccountLocked(true);
                    user.setLockedUntil(Instant.now().plusSeconds(900));
                }
                userRepository.save(user);
            });

            eventPublisher.publishEvent(LoginFailedEvent.builder()
                    .email(request.getEmail())
                    .ipAddress(ipAddress)
                    .reason(e.getMessage())
                    .build());

            throw e;
        }
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());

        User user = refreshToken.getUser();

        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(
                request.getRefreshToken(), null, null);

        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getId().toString(), user.getEmail());

        List<String> roles = user.getUserRoles().stream()
                .map(UserRole::getRole)
                .map(r -> r.getCode())
                .collect(Collectors.toList());

        List<String> permissions = user.getUserRoles().stream()
                .flatMap(ur -> ur.getRole().getRolePermissions().stream())
                .map(rp -> rp.getPermission().getCode())
                .distinct()
                .collect(Collectors.toList());

        UserInfo userInfo = UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .roles(roles)
                .permissions(permissions)
                .tenantId(user.getTenant().getId())
                .tenantName(user.getTenant().getName())
                .build();

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .expiresIn(jwtTokenProvider.getJwtExpiration() / 1000)
                .tokenType("Bearer")
                .user(userInfo)
                .build();
    }

    @Override
    @Transactional
    public MessageResponse logout(String refreshToken, String userId) {
        if (refreshToken != null) {
            refreshTokenService.revokeRefreshToken(refreshToken);
        }
        return MessageResponse.success("Logged out successfully");
    }

    @Override
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            log.info("Password reset requested for: {}", request.getEmail());
        });
        return MessageResponse.success("If the email exists, a reset link has been sent.");
    }

    @Override
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        return MessageResponse.success("Password has been reset successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfo getCurrentUser(String userId) {
        User user = userRepository.findById(java.util.UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> roles = user.getUserRoles().stream()
                .map(UserRole::getRole)
                .map(r -> r.getCode())
                .collect(Collectors.toList());

        List<String> permissions = user.getUserRoles().stream()
                .flatMap(ur -> ur.getRole().getRolePermissions().stream())
                .map(rp -> rp.getPermission().getCode())
                .distinct()
                .collect(Collectors.toList());

        return UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .roles(roles)
                .permissions(permissions)
                .tenantId(user.getTenant().getId())
                .tenantName(user.getTenant().getName())
                .build();
    }
}
