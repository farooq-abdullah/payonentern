package com.learning.util;

import com.learning.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginPolicyTest {
    @Test
    void fifthFailureLocksTheAccountInTheConfiguredWindow() {
        User user = new User();
        user.setFailedLoginCount(4);
        user.setFailedLoginWindowStartedAt(LocalDateTime.now().minusMinutes(2));

        LoginPolicy.LoginFailure failure = LoginPolicy.nextFailure(user, LocalDateTime.now());

        assertEquals(5, failure.count());
        assertTrue(failure.locksAccount());
        assertTrue(failure.lockedUntil().isAfter(LocalDateTime.now()));
    }

    @Test
    void oldFailuresStartANewWindow() {
        User user = new User();
        user.setFailedLoginCount(4);
        user.setFailedLoginWindowStartedAt(LocalDateTime.now().minusMinutes(16));

        LoginPolicy.LoginFailure failure = LoginPolicy.nextFailure(user, LocalDateTime.now());

        assertEquals(1, failure.count());
        assertFalse(failure.locksAccount());
    }
}
