package com.learning.servlet;

import com.learning.dao.HibernateRoleDao;
import com.learning.dao.RoleDao;
import com.learning.util.PermissionAccess;
import com.learning.util.Permissions;
import com.learning.util.RoleInputValidator;
import com.learning.service.AuditService;
import com.learning.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@WebServlet("/roles")
public class RolesServlet extends HttpServlet {
    private final RoleDao roleDao = new HibernateRoleDao();
    private final AuditService auditService = new AuditService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!PermissionAccess.require(request, response, Permissions.MANAGE_ROLES)) {
            return;
        }
        showPage(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!PermissionAccess.require(request, response, Permissions.MANAGE_ROLES)) {
            return;
        }

        String roleName = trimmedParameter(request, "roleName");
        Set<String> functionCodes = selectedFunctionCodes(request);
        String error = RoleInputValidator.validationError(roleName);
        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("roleName", roleName);
            request.setAttribute("selectedFunctions", selectedFunctionMap(functionCodes));
            showPage(request, response);
            return;
        }

        try {
            roleDao.create(roleName, functionCodes);
            Long roleId = roleDao.findByName(roleName).map(role -> role.getId()).orElse(null);
            auditService.record((User) request.getAttribute("signedInUser"), "ROLE_CREATED", "ROLE", roleId, roleName,
                    true, "Assigned " + functionCodes.size() + " functions");
            response.sendRedirect(request.getContextPath() + "/roles?message=roleCreated");
        } catch (SQLException exception) {
            throw new ServletException("Could not create role", exception);
        }
    }

    private void showPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("roles", roleDao.findAll());
            request.setAttribute("functions", roleDao.findAllFunctions());
            request.getRequestDispatcher("/WEB-INF/views/roles.jsp").forward(request, response);
        } catch (SQLException exception) {
            throw new ServletException("Could not load roles", exception);
        }
    }

    static Set<String> selectedFunctionCodes(HttpServletRequest request) {
        String[] values = request.getParameterValues("functions");
        if (values == null) {
            return Set.of();
        }
        Set<String> selected = new HashSet<>();
        java.util.Collections.addAll(selected, values);
        return selected;
    }

    static Map<String, Boolean> selectedFunctionMap(Set<String> functionCodes) {
        Map<String, Boolean> selected = new HashMap<>();
        for (String functionCode : functionCodes) {
            selected.put(functionCode, true);
        }
        return selected;
    }

    private String trimmedParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }
}
