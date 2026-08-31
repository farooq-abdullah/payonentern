package com.learning.dao;

import com.learning.model.AuditLogEntry;

import java.util.List;

public record AuditLogPage(List<AuditLogEntry> entries, long totalEntries, int page, int totalPages) {
}
