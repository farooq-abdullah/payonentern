package com.learning.api;

import com.learning.dao.AuditLogPage;
import com.learning.dao.UserPage;
import com.learning.model.AuditLogEntry;
import com.learning.model.Permission;
import com.learning.model.Role;
import com.learning.model.User;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Explicit response mapping prevents ORM entities and password/internal fields leaking to JSON. */
public final class ApiViewMapper {
    private ApiViewMapper() {
    }

    public static Map<String, Object> user(User user) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("role", user.getRole() == null ? null : roleSummary(user.getRole()));
        response.put("createdAt", user.getCreatedAt());
        return response;
    }

    public static Map<String, Object> userPage(UserPage page) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", page.users().stream().map(ApiViewMapper::user).toList());
        response.put("page", page.page());
        response.put("totalPages", page.totalPages());
        response.put("totalItems", page.totalUsers());
        return response;
    }

    public static Map<String, Object> role(Role role) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", role.getId());
        response.put("name", role.getName());
        response.put("defaultRole", role.isDefaultRole());
        response.put("functions", role.getFunctions().stream().map(Permission::getCode).sorted().toList());
        return response;
    }

    public static Map<String, Object> roleSummary(Role role) {
        return Map.of("id", role.getId(), "name", role.getName());
    }

    public static List<Map<String, Object>> roles(List<Role> roles) {
        return roles.stream().map(ApiViewMapper::role).toList();
    }

    public static Map<String, Object> auditPage(AuditLogPage page) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", page.entries().stream().map(ApiViewMapper::auditEntry).toList());
        response.put("page", page.page());
        response.put("totalPages", page.totalPages());
        response.put("totalItems", page.totalEntries());
        return response;
    }

    private static Map<String, Object> auditEntry(AuditLogEntry entry) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", entry.getId());
        response.put("actorUserId", entry.getActorUserId());
        response.put("actorUsername", entry.getActorUsername());
        response.put("action", entry.getAction());
        response.put("targetType", entry.getTargetType());
        response.put("targetId", entry.getTargetId());
        response.put("targetLabel", entry.getTargetLabel());
        response.put("successful", entry.isSuccessful());
        response.put("details", entry.getDetails());
        response.put("createdAt", entry.getCreatedAt());
        return response;
    }
}
