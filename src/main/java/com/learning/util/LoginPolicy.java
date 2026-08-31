package com.learning.util;

import com.learning.model.User;

import java.time.LocalDateTime;

public final class LoginPolicy {
    public static final int MAXIMUM_FAILURES = 5;
    public static final int FAILURE_WINDOW_MINUTES = 15;
    public static final int LOCKOUT_MINUTES = 15;

    private LoginPolicy() {
    }

    public static boolean isLocked(User user, LocalDateTime now) {
        return user.getLockedUntil() != null && user.getLockedUntil().isAfter(now);
    }

    public static LoginFailure nextFailure(User user, LocalDateTime now) {
        boolean insideWindow = user.getFailedLoginWindowStartedAt() != null
                && !user.getFailedLoginWindowStartedAt().isBefore(now.minusMinutes(FAILURE_WINDOW_MINUTES));
        int count = insideWindow ? user.getFailedLoginCount() + 1 : 1;
        LocalDateTime windowStartedAt = insideWindow ? user.getFailedLoginWindowStartedAt() : now;
        LocalDateTime lockedUntil = count >= MAXIMUM_FAILURES ? now.plusMinutes(LOCKOUT_MINUTES) : null;
        return new LoginFailure(count, windowStartedAt, lockedUntil);
    }

    public record LoginFailure(int count, LocalDateTime windowStartedAt, LocalDateTime lockedUntil) {
        public boolean locksAccount() {
            return lockedUntil != null;
        }
    }
}
