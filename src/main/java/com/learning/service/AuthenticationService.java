package com.learning.service;

import com.learning.dao.HibernateUserDao;
import com.learning.dao.UserDao;
import com.learning.model.User;
import com.learning.util.LoginPolicy;
import com.learning.util.PasswordHasher;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public class AuthenticationService {
    private final UserDao userDao;
    private final AuditService auditService;

    public AuthenticationService() {
        this(new HibernateUserDao(), new AuditService());
    }

    AuthenticationService(UserDao userDao, AuditService auditService) {
        this.userDao = userDao;
        this.auditService = auditService;
    }

    public AuthenticationResult authenticate(String username, String password) throws SQLException {
        Optional<User> found = userDao.findByUsername(username);
        if (found.isEmpty()) {
            auditService.record((Long) null, username, "LOGIN_FAILED", "USER", null, username, false, "Unknown username");
            return new AuthenticationResult(AuthenticationResult.Status.INVALID_CREDENTIALS, null);
        }

        User user = found.get();
        LocalDateTime now = LocalDateTime.now();
        if (LoginPolicy.isLocked(user, now)) {
            auditService.record(user, "LOGIN_LOCKED", "USER", user.getId(), user.getUsername(), false,
                    "Account is locked until " + user.getLockedUntil());
            return new AuthenticationResult(AuthenticationResult.Status.LOCKED, null);
        }

        if (!PasswordHasher.matches(password, user.getPasswordHash())) {
            LoginPolicy.LoginFailure failure = LoginPolicy.nextFailure(user, now);
            userDao.updateLoginFailure(user.getId(), failure.count(), failure.windowStartedAt(), failure.lockedUntil());
            auditService.record(user, failure.locksAccount() ? "LOGIN_LOCKED" : "LOGIN_FAILED", "USER", user.getId(),
                    user.getUsername(), false, failure.locksAccount() ? "Maximum failed attempts reached" : "Invalid password");
            return new AuthenticationResult(failure.locksAccount()
                    ? AuthenticationResult.Status.LOCKED : AuthenticationResult.Status.INVALID_CREDENTIALS, null);
        }

        userDao.clearLoginFailures(user.getId());
        auditService.record(user, "LOGIN_SUCCESS", "USER", user.getId(), user.getUsername(), true, null);
        return new AuthenticationResult(AuthenticationResult.Status.SUCCESS, user);
    }

    public boolean unlock(User actor, long userId) throws SQLException {
        Optional<User> target = userDao.findById(userId);
        if (target.isEmpty()) return false;
        userDao.unlock(userId);
        auditService.record(actor, "ACCOUNT_UNLOCKED", "USER", userId, target.get().getUsername(), true, null);
        return true;
    }
}
