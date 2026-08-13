package com.learning.util;

import com.learning.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordPolicyTest {
    @Test
    void rejectsCurrentAndRecentPasswords() {
        User user = new User();
        user.setPasswordHash(PasswordHasher.hash("Current1!"));
        List<String> history = List.of(PasswordHasher.hash("Previous1!"));

        assertTrue(PasswordPolicy.matchesCurrentOrRecentPassword("Current1!", user, history));
        assertTrue(PasswordPolicy.matchesCurrentOrRecentPassword("Previous1!", user, history));
        assertFalse(PasswordPolicy.matchesCurrentOrRecentPassword("Different1!", user, history));
    }

    @Test
    void detectsMissingAndExpiredPasswordDates() {
        User user = new User();
        assertTrue(PasswordPolicy.isExpired(user));

        user.setPasswordChangedAt(LocalDateTime.now());
        assertFalse(PasswordPolicy.isExpired(user));

        user.setPasswordChangedAt(LocalDateTime.now().minusDays(91));
        assertTrue(PasswordPolicy.isExpired(user));
    }
}
