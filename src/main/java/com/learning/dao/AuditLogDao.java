package com.learning.dao;

import com.learning.model.AuditLogEntry;

import java.sql.SQLException;

public interface AuditLogDao {
    void create(AuditLogEntry entry) throws SQLException;

    AuditLogPage findPage(AuditLogFilter filter) throws SQLException;
}
