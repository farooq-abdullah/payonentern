package com.learning.service;

import com.learning.dao.HibernatePasswordResetTokenDao;
import com.learning.dao.HibernateUserDao;
import com.learning.dao.PasswordResetTokenDao;
import com.learning.dao.UserDao;
import com.learning.model.PasswordResetToken;
import com.learning.model.User;
import com.learning.util.PasswordHasher;
import com.learning.util.PasswordPolicy;
import com.learning.util.ResetTokenHasher;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ForgotPasswordService {
    private static final int TOKEN_MINUTES = 15;
    private final UserDao userDao;
    private final PasswordResetTokenDao tokenDao;
    private final MailService mailService;
    private final AuditService auditService;

    public ForgotPasswordService() {
        this(new HibernateUserDao(), new HibernatePasswordResetTokenDao(), new MailService(), new AuditService());
    }

    ForgotPasswordService(UserDao userDao, PasswordResetTokenDao tokenDao, MailService mailService, AuditService auditService) {
        this.userDao = userDao;
        this.tokenDao = tokenDao;
        this.mailService = mailService;
        this.auditService = auditService;
    }

    public void requestReset(String email, String resetLinkPrefix) throws SQLException {
        Optional<User> found = userDao.findByEmail(email);
        if (found.isEmpty()) {
            auditService.record((Long) null, email, "PASSWORD_RESET_REQUEST", "USER", null, email, false,
                    "No account matched the email");
            return;
        }
        User user = found.get();
        String token = ResetTokenHasher.generateToken();
        tokenDao.createForUser(user.getId(), ResetTokenHasher.hash(token), LocalDateTime.now().plusMinutes(TOKEN_MINUTES));
        try {
            mailService.sendPasswordReset(user.getEmail(), resetLinkPrefix + token);
            auditService.record(user, "PASSWORD_RESET_REQUEST", "USER", user.getId(), user.getUsername(), true, null);
        } catch (Exception exception) {
            auditService.record(user, "PASSWORD_RESET_EMAIL_FAILED", "USER", user.getId(), user.getUsername(), false,
                    "Mail delivery failed");
            throw new SQLException("Could not send reset email", exception);
        }
    }

    public PasswordOperationResult resetPassword(String rawToken, String newPassword, String confirmation) throws SQLException {
        if (rawToken == null || rawToken.isBlank()) return PasswordOperationResult.failure("This reset link is invalid or expired.");
        Optional<PasswordResetToken> token = tokenDao.findValidByHash(ResetTokenHasher.hash(rawToken));
        if (token.isEmpty()) return PasswordOperationResult.failure("This reset link is invalid or expired.");
        Optional<User> found = userDao.findById(token.get().getUserId());
        if (found.isEmpty()) return PasswordOperationResult.failure("This reset link is invalid or expired.");
        User user = found.get();
        if (newPassword == null || newPassword.isBlank() || confirmation == null || confirmation.isBlank()) {
            return PasswordOperationResult.failure("New password and confirmation are required.");
        }
        if (!newPassword.equals(confirmation)) return PasswordOperationResult.failure("New password and confirmation do not match.");
        String passwordError = PasswordPolicy.validationError(newPassword);
        if (passwordError != null) return PasswordOperationResult.failure(passwordError);
        List<String> history = userDao.findRecentPasswordHashes(user.getId(), 4);
        if (PasswordPolicy.matchesCurrentOrRecentPassword(newPassword, user, history)) {
            return PasswordOperationResult.failure("New password cannot match your current or previous four passwords.");
        }
        Optional<User> updated = tokenDao.consumeAndUpdatePassword(ResetTokenHasher.hash(rawToken), PasswordHasher.hash(newPassword));
        if (updated.isEmpty()) return PasswordOperationResult.failure("This reset link is invalid or expired.");
        auditService.record(updated.get(), "PASSWORD_RESET_SELF_SERVICE", "USER", updated.get().getId(),
                updated.get().getUsername(), true, null);
        return PasswordOperationResult.success();
    }
}
