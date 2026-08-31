package com.learning.dao;

public record AuditLogFilter(String action, String actor, String targetType, Boolean successful, int page, int pageSize) {
}
