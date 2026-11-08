package com.orientation.orientationapp.modules.auth.service;
import com.orientation.orientationapp.modules.auth.repository.UserRepository;
import com.orientation.orientationapp.modules.auth.service.impl.AuthenticationServiceImpl;
import com.orientation.orientationapp.modules.auth.dto.response.LoginResponse;
import com.orientation.orientationapp.security.service.AccountState;
import com.orientation.orientationapp.security.jwt.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
@Service @RequiredArgsConstructor
public class LoginSessionIssuer {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final RefreshTokenService sessions;
    private final JwtTokenProvider jwt;
    @Transactional
    public LoginResponse issue(String email,String password,String ip,String agent) {
        var user=users.lockByEmail(email).orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if(!AccountState.active(user) || !encoder.matches(password,user.getPassword()))
            throw new BadCredentialsException("Invalid credentials");
        user.setFailedLoginAttempts(0);user.setAccountLocked(false);user.setLockedUntil(null);
        var session=sessions.createRefreshToken(user,ip,agent);
        return LoginResponse.builder().accessToken(jwt.generateAccessToken(user.getId().toString(),null,session.getFamilyId()))
            .refreshToken(session.getPlainToken()).expiresIn(jwt.getJwtExpiration()/1000)
            .tokenType("Bearer").user(AuthenticationServiceImpl.userInfo(user)).build();
    }
}
