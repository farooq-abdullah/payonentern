package com.learning.service;

import com.learning.model.User;

public record AuthenticationResult(Status status, User user) {
    public enum Status { SUCCESS, INVALID_CREDENTIALS, LOCKED }
}
