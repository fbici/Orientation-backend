package com.orientation.orientationapp.modules.auth.service.impl;

import com.orientation.orientationapp.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    private final UserRepository users;

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void failed(String email) {
        users.lockByEmail(email).ifPresent(user -> {
            Instant now=Instant.now();
            if (user.getLockedUntil()!=null && user.getLockedUntil().isAfter(now)) return;
            int count = user.getLockedUntil()!=null ? 1 : user.getFailedLoginAttempts()+1;
            user.setFailedLoginAttempts(count);
            if (count>=5) {
                user.setAccountLocked(true);
                user.setLockedUntil(now.plusSeconds(900));
            }
            users.save(user);
        });
    }
}
