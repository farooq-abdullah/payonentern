package com.learning.util;

import java.util.regex.Pattern;

public final class RoleInputValidator {
    private static final Pattern ROLE_NAME_PATTERN = Pattern.compile("[A-Za-z0-9 _-]{3,50}");

    private RoleInputValidator() {
    }

    public static String validationError(String name) {
        if (name == null || !ROLE_NAME_PATTERN.matcher(name).matches()) {
            return "Role names must be 3 to 50 characters and use only letters, numbers, spaces, underscores, or hyphens.";
        }
        return null;
    }
}
