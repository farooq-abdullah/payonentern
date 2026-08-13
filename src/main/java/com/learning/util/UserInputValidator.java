package com.learning.util;

import java.util.regex.Pattern;

public final class UserInputValidator {
    private static final int MINIMUM_USERNAME_LENGTH = 3;
    private static final int MAXIMUM_USERNAME_LENGTH = 50;
    private static final int MAXIMUM_EMAIL_LENGTH = 254;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9._-]+");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,63}");

    private UserInputValidator() {
    }

    public static String validationError(String username, String email) {
        if (username == null || username.isBlank() || email == null || email.isBlank()) {
            return "Username and email are required.";
        }

        if (username.length() < MINIMUM_USERNAME_LENGTH
                || username.length() > MAXIMUM_USERNAME_LENGTH
                || !USERNAME_PATTERN.matcher(username).matches()) {
            return "Username must be 3 to 50 characters and use only letters, numbers, dots, underscores, or hyphens.";
        }

        if (email.length() > MAXIMUM_EMAIL_LENGTH || !EMAIL_PATTERN.matcher(email).matches()) {
            return "Enter a valid email address.";
        }

        return null;
    }
}
