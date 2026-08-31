package com.learning.dao;

import com.learning.model.Permission;
import com.learning.model.Role;
import com.learning.util.HibernateUtil;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class HibernateRoleDao implements RoleDao {
    private final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    @Override
    public List<Role> findAll() throws SQLException {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from Role role order by role.name", Role.class).getResultList();
        } catch (HibernateException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public Optional<Role> findById(long roleId) throws SQLException {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.find(Role.class, roleId));
        } catch (HibernateException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public Optional<Role> findByName(String roleName) throws SQLException {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from Role role where role.name = :roleName", Role.class)
                    .setParameter("roleName", roleName)
                    .uniqueResultOptional();
        } catch (HibernateException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public Optional<Role> findDefaultRole() throws SQLException {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from Role role where role.defaultRole = true", Role.class)
                    .uniqueResultOptional();
        } catch (HibernateException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public List<Permission> findAllFunctions() throws SQLException {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from Permission permission order by permission.code", Permission.class)
                    .getResultList();
        } catch (HibernateException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public Set<String> findAllFunctionCodes() throws SQLException {
        try (Session session = sessionFactory.openSession()) {
            return new HashSet<>(session.createQuery(
                            "select permission.code from Permission permission", String.class)
                    .getResultList());
        } catch (HibernateException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public boolean create(String name, Set<String> functionCodes) throws SQLException {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            Role role = new Role();
            role.setName(name);
            role.setFunctions(resolveFunctions(session, functionCodes));
            session.persist(role);
            transaction.commit();
            return true;
        } catch (HibernateException | IllegalArgumentException exception) {
            rollback(transaction);
            throw databaseException(exception);
        }
    }

    @Override
    public boolean update(long roleId, String name, Set<String> functionCodes) throws SQLException {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            Role role = session.find(Role.class, roleId);
            if (role == null) {
                transaction.commit();
                return false;
            }
            role.setName(name);
            role.setFunctions(resolveFunctions(session, functionCodes));
            transaction.commit();
            return true;
        } catch (HibernateException | IllegalArgumentException exception) {
            rollback(transaction);
            throw databaseException(exception);
        }
    }

    @Override
    public boolean deleteById(long roleId) throws SQLException {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            Role role = session.find(Role.class, roleId);
            if (role == null) {
                transaction.commit();
                return false;
            }
            session.remove(role);
            transaction.commit();
            return true;
        } catch (HibernateException exception) {
            rollback(transaction);
            throw databaseException(exception);
        }
    }

    private Set<Permission> resolveFunctions(Session session, Set<String> functionCodes) {
        if (functionCodes.isEmpty()) {
            return Set.of();
        }

        List<Permission> functions = session.createQuery(
                        "from Permission permission where permission.code in :codes", Permission.class)
                .setParameter("codes", functionCodes)
                .getResultList();
        if (functions.size() != functionCodes.size()) {
            throw new IllegalArgumentException("An unknown system function was selected");
        }
        return new HashSet<>(functions);
    }

    private void rollback(Transaction transaction) {
        if (transaction != null && transaction.isActive()) {
            transaction.rollback();
        }
    }

    private SQLException databaseException(Exception exception) {
        return new SQLException("Role operation failed", exception);
    }
}
