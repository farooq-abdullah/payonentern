package com.learning.util;

import com.learning.model.User;

import java.time.LocalDateTime;

public final class PasswordPolicy {
    private static final int MINIMUM_LENGTH = 8;
    private static final int EXPIRY_DAYS = 90;

    private PasswordPolicy() {
    }

    public static String validationError(String password) {
        if (password == null || password.length() < MINIMUM_LENGTH) {
            return "Password must contain at least 8 characters.";
        }

        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecialCharacter = password.chars()
                .anyMatch(character -> !Character.isLetterOrDigit(character) && !Character.isWhitespace(character));

        if (!hasUppercase || !hasLowercase || !hasDigit || !hasSpecialCharacter) {
            return "Password must include uppercase, lowercase, digit, and special characters.";
        }

        return null;
    }

    public static boolean isExpired(User user) {
        return user.getPasswordChangedAt() == null
                || user.getPasswordChangedAt().isBefore(LocalDateTime.now().minusDays(EXPIRY_DAYS));
    }

}
