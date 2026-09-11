package com.learning.service;

/**
 * A small, transport-neutral result used by services. Servlets and REST endpoints
 * translate the same outcome into HTML redirects or HTTP/JSON responses.
 */
public record ServiceResult<T>(Status status, T value, String error) {
    public enum Status {
        SUCCESS,
        VALIDATION_ERROR,
        CONFLICT,
        NOT_FOUND,
        PROTECTED
    }

    public static <T> ServiceResult<T> success(T value) {
        return new ServiceResult<>(Status.SUCCESS, value, null);
    }

    public static <T> ServiceResult<T> validationError(String error) {
        return new ServiceResult<>(Status.VALIDATION_ERROR, null, error);
    }

    public static <T> ServiceResult<T> conflict(String error) {
        return new ServiceResult<>(Status.CONFLICT, null, error);
    }

    public static <T> ServiceResult<T> notFound(String error) {
        return new ServiceResult<>(Status.NOT_FOUND, null, error);
    }

    public static <T> ServiceResult<T> protectedOperation(String error) {
        return new ServiceResult<>(Status.PROTECTED, null, error);
    }

    public boolean successful() {
        return status == Status.SUCCESS;
    }
}
