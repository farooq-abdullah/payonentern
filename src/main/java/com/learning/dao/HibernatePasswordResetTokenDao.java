package com.learning.dao;

import com.learning.model.PasswordHistory;
import com.learning.model.PasswordResetToken;
import com.learning.model.User;
import com.learning.util.HibernateUtil;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import jakarta.persistence.LockModeType;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public class HibernatePasswordResetTokenDao implements PasswordResetTokenDao {
    private final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    @Override
    public void createForUser(long userId, String tokenHash, LocalDateTime expiresAt) throws SQLException {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.createMutationQuery("update PasswordResetToken token set token.usedAt = :now "
                            + "where token.userId = :userId and token.usedAt is null")
                    .setParameter("now", LocalDateTime.now())
                    .setParameter("userId", userId)
                    .executeUpdate();
            session.persist(new PasswordResetToken(userId, tokenHash, expiresAt));
            transaction.commit();
        } catch (HibernateException exception) {
            rollback(transaction);
            throw new SQLException("Could not store password reset token", exception);
        }
    }

    @Override
    public Optional<PasswordResetToken> findValidByHash(String tokenHash) throws SQLException {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from PasswordResetToken token where token.tokenHash = :tokenHash "
                            + "and token.usedAt is null and token.expiresAt > :now", PasswordResetToken.class)
                    .setParameter("tokenHash", tokenHash)
                    .setParameter("now", LocalDateTime.now())
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .uniqueResultOptional();
        } catch (HibernateException exception) {
            throw new SQLException("Could not read password reset token", exception);
        }
    }

    @Override
    public Optional<User> consumeAndUpdatePassword(String tokenHash, String passwordHash) throws SQLException {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            Optional<PasswordResetToken> token = session.createQuery(
                            "from PasswordResetToken token where token.tokenHash = :tokenHash "
                                    + "and token.usedAt is null and token.expiresAt > :now", PasswordResetToken.class)
                    .setParameter("tokenHash", tokenHash)
                    .setParameter("now", LocalDateTime.now())
                    .uniqueResultOptional();
            if (token.isEmpty()) {
                transaction.commit();
                return Optional.empty();
            }
            User user = session.find(User.class, token.get().getUserId());
            if (user == null) {
                transaction.commit();
                return Optional.empty();
            }
            session.persist(new PasswordHistory(user.getId(), user.getPasswordHash()));
            user.setPasswordHash(passwordHash);
            user.setPasswordChangedAt(LocalDateTime.now());
            user.setMustChangePassword(false);
            token.get().setUsedAt(LocalDateTime.now());
            transaction.commit();
            return Optional.of(user);
        } catch (HibernateException exception) {
            rollback(transaction);
            throw new SQLException("Could not use password reset token", exception);
        }
    }

    private void rollback(Transaction transaction) {
        if (transaction != null && transaction.isActive()) transaction.rollback();
    }
}
