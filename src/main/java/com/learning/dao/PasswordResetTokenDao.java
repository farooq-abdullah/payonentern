package com.learning.dao;

import com.learning.model.PasswordResetToken;
import com.learning.model.User;

import java.sql.SQLException;
import java.util.Optional;

public interface PasswordResetTokenDao {
    void createForUser(long userId, String tokenHash, java.time.LocalDateTime expiresAt) throws SQLException;

    Optional<PasswordResetToken> findValidByHash(String tokenHash) throws SQLException;

    Optional<User> consumeAndUpdatePassword(String tokenHash, String passwordHash) throws SQLException;
}
