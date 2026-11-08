package com.orientation.orientationapp.modules.auth.service.impl;

import com.orientation.orientationapp.modules.auth.dto.request.*;
import com.orientation.orientationapp.modules.auth.dto.response.*;
import com.orientation.orientationapp.modules.auth.entity.*;
import com.orientation.orientationapp.modules.auth.repository.UserRepository;
import com.orientation.orientationapp.modules.auth.service.*;
import com.orientation.orientationapp.security.jwt.JwtTokenProvider;
import com.orientation.orientationapp.security.service.AccountState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final LoginSessionIssuer issuer;
    private final LoginAttemptService attempts;
    private final com.orientation.orientationapp.modules.auth.repository.RefreshTokenRepository credentials;
    private final AccountRecoveryService recovery;

    @Override
    public LoginResponse login(LoginRequest request, String ip, String agent) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));
            return issuer.issue(request.getEmail(),request.getPassword(),ip,agent);
        } catch (org.springframework.security.core.AuthenticationException ex) {
            attempts.failed(request.getEmail());
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    @Override @Transactional(noRollbackFor=InvalidRefreshException.class)
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        return response(refreshTokenService.rotateRefreshToken(request.getRefreshToken(),null,null));
    }
    private LoginResponse response(RefreshToken session) {
        User user=session.getUser();
        return LoginResponse.builder().accessToken(jwtTokenProvider.generateAccessToken(
                user.getId().toString(),null,session.getFamilyId()))
                .refreshToken(session.getPlainToken()).expiresIn(jwtTokenProvider.getJwtExpiration()/1000)
                .tokenType("Bearer").user(userInfo(user)).build();
    }
    @Override @Transactional
    public MessageResponse logout(String token,String userId) {
        if (userId==null || token==null) throw new BadCredentialsException("Invalid session");
        if(!token.matches("rt1_[A-Za-z0-9_-]{43}")) throw new BadCredentialsException("Invalid session");
        UUID owner=credentials.findUserIdByToken(RefreshTokenServiceImpl.digest(token))
            .orElseThrow(() -> new BadCredentialsException("Invalid session"));
        if (!owner.toString().equals(userId))
            throw new BadCredentialsException("Invalid session");
        refreshTokenService.revokeRefreshToken(token);
        return MessageResponse.success("Logged out successfully");
    }
    @Override
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        recovery.requestReset(request.getEmail());
        return MessageResponse.success("If eligible, password recovery instructions will be delivered.");
    }
    @Override
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        recovery.reset(request.getToken(),request.getNewPassword());
        return MessageResponse.success("Password reset. Please sign in again.");
    }
    @Override @Transactional(readOnly=true)
    public UserInfo getCurrentUser(String userId) {
        User user=userRepository.findById(UUID.fromString(userId))
                .filter(AccountState::active).orElseThrow(() -> new BadCredentialsException("Invalid session"));
        return userInfo(user);
    }
    public static UserInfo userInfo(User user) {
        List<Role> roles=user.getUserRoles().stream().map(UserRole::getRole)
                .filter(r -> Boolean.TRUE.equals(r.getActive()) && !Boolean.TRUE.equals(r.getDeleted())).toList();
        return UserInfo.builder().id(user.getId()).email(user.getEmail()).firstName(user.getFirstName())
                .lastName(user.getLastName()).fullName(user.getFullName())
                .roles(roles.stream().map(Role::getCode).toList())
                .permissions(roles.stream().flatMap(r -> r.getRolePermissions().stream()).map(RolePermission::getPermission)
                    .filter(p -> Boolean.TRUE.equals(p.getActive()) && !Boolean.TRUE.equals(p.getDeleted()))
                    .map(Permission::getCode).distinct().toList())
                .tenantId(user.getTenant().getId()).tenantName(user.getTenant().getName()).build();
    }
}
