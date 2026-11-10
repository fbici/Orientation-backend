package com.orientation.orientationapp.modules.auth.service;
import com.orientation.orientationapp.modules.auth.repository.UserRepository;
import com.orientation.orientationapp.modules.auth.dto.request.ChangePasswordRequest;
import com.orientation.orientationapp.modules.auth.dto.response.UserInfo;
import com.orientation.orientationapp.modules.auth.service.impl.AuthenticationServiceImpl;
import com.orientation.orientationapp.security.service.ResourceAccess;
import com.orientation.orientationapp.exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.Instant;
@Service @RequiredArgsConstructor @Transactional
public class AccountProfileService {
    private final InternationalPhoneService phones;
    private final jakarta.persistence.EntityManager entityManager;
    private final ResourceAccess access;private final UserRepository users;
    private final PasswordEncoder encoder;private final RefreshTokenService sessions;
    public UserInfo update(String first,String last,String phone) {
        var user=users.lockById(access.current().getId()).orElseThrow(ResourceAccess::denied);
        user.setFirstName(first);user.setLastName(last);user.setPhone(phones.normalize(phone));
        return AuthenticationServiceImpl.userInfo(users.save(user));
    }
    public void password(ChangePasswordRequest request) {
        var user=users.lockById(access.current().getId()).orElseThrow(ResourceAccess::denied);
        entityManager.refresh(user,jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        if(!encoder.matches(request.getCurrentPassword(),user.getPassword()))
            throw new BusinessException("Invalid current password","INVALID_PASSWORD");
        if(request.getNewPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
            throw new BusinessException("Password exceeds 72 bytes","INVALID_PASSWORD");
        user.setPassword(encoder.encode(request.getNewPassword()));user.setPasswordChangedAt(Instant.now());
        users.saveAndFlush(user);sessions.revokeAllUserTokens(user.getId());
    }
}
