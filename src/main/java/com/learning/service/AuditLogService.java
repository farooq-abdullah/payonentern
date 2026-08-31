package com.learning.service;

import com.learning.dao.AuditLogDao;
import com.learning.dao.AuditLogFilter;
import com.learning.dao.AuditLogPage;
import com.learning.dao.HibernateAuditLogDao;

import java.sql.SQLException;

public class AuditLogService {
    private static final int PAGE_SIZE = 20;
    private final AuditLogDao auditLogDao;

    public AuditLogService() {
        this(new HibernateAuditLogDao());
    }

    AuditLogService(AuditLogDao auditLogDao) {
        this.auditLogDao = auditLogDao;
    }

    public AuditLogPage findEntries(String action, String actor, String targetType, String successful, String pageParameter)
            throws SQLException {
        Boolean successfulFilter = "true".equals(successful) ? Boolean.TRUE : "false".equals(successful) ? Boolean.FALSE : null;
        return auditLogDao.findPage(new AuditLogFilter(value(action), value(actor), value(targetType), successfulFilter,
                page(pageParameter), PAGE_SIZE));
    }

    private String value(String input) {
        return input == null ? "" : input.trim();
    }

    private int page(String input) {
        try {
            int page = Integer.parseInt(input);
            return page > 0 ? page : 1;
        } catch (NumberFormatException | NullPointerException exception) {
            return 1;
        }
    }
}
