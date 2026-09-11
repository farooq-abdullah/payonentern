package com.learning.service;

public record PasswordOperationResult(Status status, String error) {
    public enum Status {
        SUCCESS,
        VALIDATION_ERROR,
        NOT_FOUND,
        INVALID_TOKEN
    }

    public boolean successful() {
        return status == Status.SUCCESS;
    }

    public static PasswordOperationResult success() {
        return new PasswordOperationResult(Status.SUCCESS, null);
    }

    public static PasswordOperationResult failure(String error) {
        return new PasswordOperationResult(Status.VALIDATION_ERROR, error);
    }

    public static PasswordOperationResult notFound(String error) {
        return new PasswordOperationResult(Status.NOT_FOUND, error);
    }

    public static PasswordOperationResult invalidToken(String error) {
        return new PasswordOperationResult(Status.INVALID_TOKEN, error);
    }
}
