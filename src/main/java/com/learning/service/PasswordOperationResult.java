package com.learning.service;

public record PasswordOperationResult(boolean successful, String error) {
    public static PasswordOperationResult success() {
        return new PasswordOperationResult(true, null);
    }

    public static PasswordOperationResult failure(String error) {
        return new PasswordOperationResult(false, error);
    }
}
