package com.learning.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordHasher {
    private static final int WORK_FACTOR = 12;

    private PasswordHasher() {
    }

    public static String hash(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    public static boolean matches(String plainTextPassword, String storedHash) {
        if (plainTextPassword == null || storedHash == null) {
            return false;
        }
        return BCrypt.checkpw(plainTextPassword, storedHash);
    }
}
