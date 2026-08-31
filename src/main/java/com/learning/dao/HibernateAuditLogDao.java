package com.learning.dao;

import com.learning.model.AuditLogEntry;
import com.learning.util.HibernateUtil;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HibernateAuditLogDao implements AuditLogDao {
    private final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    @Override
    public void create(AuditLogEntry entry) throws SQLException {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            session.persist(entry);
            session.getTransaction().commit();
        } catch (HibernateException exception) {
            throw new SQLException("Could not write audit log", exception);
        }
    }

    @Override
    public AuditLogPage findPage(AuditLogFilter filter) throws SQLException {
        String where = whereClause(filter);
        try (Session session = sessionFactory.openSession()) {
            List<AuditLogEntry> entries = bind(session.createQuery(
                            "from AuditLogEntry entry" + where + " order by entry.createdAt desc, entry.id desc",
                            AuditLogEntry.class), filter)
                    .setFirstResult((filter.page() - 1) * filter.pageSize())
                    .setMaxResults(filter.pageSize())
                    .getResultList();
            Long total = bind(session.createQuery("select count(entry) from AuditLogEntry entry" + where, Long.class), filter)
                    .getSingleResult();
            int totalPages = Math.max(1, (int) Math.ceil(total / (double) filter.pageSize()));
            return new AuditLogPage(entries, total, filter.page(), totalPages);
        } catch (HibernateException exception) {
            throw new SQLException("Could not read audit log", exception);
        }
    }

    private String whereClause(AuditLogFilter filter) {
        List<String> conditions = new ArrayList<>();
        if (!filter.action().isBlank()) conditions.add("entry.action = :action");
        if (!filter.actor().isBlank()) conditions.add("lower(entry.actorUsername) like :actor");
        if (!filter.targetType().isBlank()) conditions.add("entry.targetType = :targetType");
        if (filter.successful() != null) conditions.add("entry.successful = :successful");
        return conditions.isEmpty() ? "" : " where " + String.join(" and ", conditions);
    }

    private <T> org.hibernate.query.Query<T> bind(org.hibernate.query.Query<T> query, AuditLogFilter filter) {
        if (!filter.action().isBlank()) query.setParameter("action", filter.action());
        if (!filter.actor().isBlank()) query.setParameter("actor", "%" + filter.actor().toLowerCase() + "%");
        if (!filter.targetType().isBlank()) query.setParameter("targetType", filter.targetType());
        if (filter.successful() != null) query.setParameter("successful", filter.successful());
        return query;
    }
}
