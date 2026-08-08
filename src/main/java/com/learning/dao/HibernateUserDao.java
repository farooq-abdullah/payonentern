package com.learning.dao;

import com.learning.model.PasswordHistory;
import com.learning.model.User;
import com.learning.util.HibernateUtil;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class HibernateUserDao implements UserDao {
    private final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    @Override
    public void create(User user) throws SQLException {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
        } catch (HibernateException exception) {
            rollback(transaction);
            throw databaseException(exception);
        }
    }

    @Override
    public boolean existsByUsernameOrEmail(String username, String email) throws SQLException {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery(
                            "select count(user) from User user where user.username = :username or user.email = :email",
                            Long.class)
                    .setParameter("username", username)
                    .setParameter("email", email)
                    .getSingleResult();
            return count > 0;
        } catch (HibernateException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) throws SQLException {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from User user where user.username = :username", User.class)
                    .setParameter("username", username)
                    .uniqueResultOptional();
        } catch (HibernateException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public Optional<User> findById(long userId) throws SQLException {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.find(User.class, userId));
        } catch (HibernateException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public List<User> findAll() throws SQLException {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from User user order by user.id", User.class).getResultList();
        } catch (HibernateException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public boolean updatePassword(long userId, String passwordHash, boolean mustChangePassword) throws SQLException {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            User user = session.find(User.class, userId);
            if (user == null) {
                transaction.commit();
                return false;
            }
            session.persist(new PasswordHistory(user.getId(), user.getPasswordHash()));
            user.setPasswordHash(passwordHash);
            user.setPasswordChangedAt(LocalDateTime.now());
            user.setMustChangePassword(mustChangePassword);
            transaction.commit();
            return true;
        } catch (HibernateException exception) {
            rollback(transaction);
            throw databaseException(exception);
        }
    }

    @Override
    public List<String> findRecentPasswordHashes(long userId, int limit) throws SQLException {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "select history.passwordHash from PasswordHistory history where history.userId = :userId order by history.createdAt desc, history.id desc",
                            String.class)
                    .setParameter("userId", userId)
                    .setMaxResults(limit)
                    .getResultList();
        } catch (HibernateException exception) {
            throw databaseException(exception);
        }
    }

    private void rollback(Transaction transaction) {
        if (transaction != null && transaction.isActive()) {
            transaction.rollback();
        }
    }

    private SQLException databaseException(HibernateException exception) {
        return new SQLException("Database operation failed", exception);
    }
}
