package com.learning.service;

import com.learning.dao.AuditLogDao;
import com.learning.dao.HibernateAuditLogDao;
import com.learning.model.AuditLogEntry;
import com.learning.model.User;

import java.sql.SQLException;

public class AuditService {
    private final AuditLogDao auditLogDao;

    public AuditService() {
        this(new HibernateAuditLogDao());
    }

    AuditService(AuditLogDao auditLogDao) {
        this.auditLogDao = auditLogDao;
    }

    public void record(User actor, String action, String targetType, Long targetId,
                       String targetLabel, boolean successful, String details) throws SQLException {
        Long actorId = actor == null ? null : actor.getId();
        String actorUsername = actor == null ? null : actor.getUsername();
        record(actorId, actorUsername, action, targetType, targetId, targetLabel, successful, details);
    }

    public void record(Long actorId, String actorUsername, String action, String targetType, Long targetId,
                       String targetLabel, boolean successful, String details) throws SQLException {
        auditLogDao.create(new AuditLogEntry(actorId, actorUsername, action, targetType, targetId,
                targetLabel, successful, details));
    }
}
