package com.orientation.orientationapp.security.service;

import com.orientation.orientationapp.modules.auth.entity.User;
import com.orientation.orientationapp.common.enums.CandidateStatus;
import java.time.Instant;

public final class AccountState {
    private AccountState() {}
    public static boolean active(User user) {
        return user != null && !Boolean.TRUE.equals(user.getDeleted())
                && Boolean.TRUE.equals(user.getEnabled())
                && user.getStatus() == CandidateStatus.ACTIVE
                && Boolean.TRUE.equals(user.getEmailVerified())
                && !(Boolean.TRUE.equals(user.getAccountLocked()) &&
                    (user.getLockedUntil() == null || user.getLockedUntil().isAfter(Instant.now())))
                && user.getTenant() != null && Boolean.TRUE.equals(user.getTenant().getActive())
                && !Boolean.TRUE.equals(user.getTenant().getDeleted())
                && user.getTenant().getOrganization() != null
                && Boolean.TRUE.equals(user.getTenant().getOrganization().getActive())
                && !Boolean.TRUE.equals(user.getTenant().getOrganization().getDeleted());
    }
}
