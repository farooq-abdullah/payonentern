package com.learning.service;

import com.learning.dao.HibernateUserDao;
import com.learning.dao.UserDao;
import com.learning.model.User;
import com.learning.util.PasswordHasher;
import com.learning.util.PasswordPolicy;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class PasswordManagementService {
    private final UserDao userDao;
    private final AuditService auditService;

    public PasswordManagementService() {
        this(new HibernateUserDao(), new AuditService());
    }

    PasswordManagementService(UserDao userDao, AuditService auditService) {
        this.userDao = userDao;
        this.auditService = auditService;
    }

    public PasswordOperationResult changeOwnPassword(long userId, String currentPassword,
                                                      String newPassword, String confirmation) throws SQLException {
        if (blank(currentPassword) || blank(newPassword) || blank(confirmation)) {
            return PasswordOperationResult.failure("All password fields are required.");
        }
        if (!newPassword.equals(confirmation)) {
            return PasswordOperationResult.failure("New password and confirmation do not match.");
        }
        Optional<User> found = userDao.findById(userId);
        if (found.isEmpty()) return PasswordOperationResult.failure("Your account no longer exists.");
        User user = found.get();
        if (!PasswordHasher.matches(currentPassword, user.getPasswordHash())) {
            auditService.record(user, "PASSWORD_CHANGE_FAILED", "USER", userId, user.getUsername(), false,
                    "Current password did not match");
            return PasswordOperationResult.failure("Current password is incorrect.");
        }
        PasswordOperationResult validation = validateNewPassword(user, newPassword, confirmation);
        if (!validation.successful()) return validation;
        userDao.updatePassword(userId, PasswordHasher.hash(newPassword), false);
        auditService.record(user, "PASSWORD_CHANGED", "USER", userId, user.getUsername(), true, null);
        return PasswordOperationResult.success();
    }

    public PasswordOperationResult resetByAdministrator(User actor, long userId, String newPassword,
                                                         String confirmation) throws SQLException {
        Optional<User> found = userDao.findById(userId);
        if (found.isEmpty()) return PasswordOperationResult.failure("User was not found.");
        User target = found.get();
        if (blank(newPassword) || blank(confirmation)) {
            return PasswordOperationResult.failure("New password and confirmation are required.");
        }
        PasswordOperationResult validation = validateNewPassword(target, newPassword, confirmation);
        if (!validation.successful()) return validation;
        userDao.updatePassword(userId, PasswordHasher.hash(newPassword), true);
        auditService.record(actor, "PASSWORD_RESET", "USER", userId, target.getUsername(), true,
                "Administrator reset; user must change it after login");
        return PasswordOperationResult.success();
    }

    private PasswordOperationResult validateNewPassword(User user, String newPassword, String confirmation) throws SQLException {
        if (!newPassword.equals(confirmation)) {
            return PasswordOperationResult.failure("New password and confirmation do not match.");
        }
        String passwordError = PasswordPolicy.validationError(newPassword);
        if (passwordError != null) return PasswordOperationResult.failure(passwordError);
        List<String> recentHashes = userDao.findRecentPasswordHashes(user.getId(), 4);
        if (PasswordPolicy.matchesCurrentOrRecentPassword(newPassword, user, recentHashes)) {
            return PasswordOperationResult.failure("New password cannot match the current or previous four passwords.");
        }
        return PasswordOperationResult.success();
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
