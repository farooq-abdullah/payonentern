package com.learning.servlet;

import com.learning.dao.AuditLogPage;
import com.learning.service.AuditLogService;
import com.learning.util.PermissionAccess;
import com.learning.util.Permissions;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/audit-log")
public class AuditLogServlet extends HttpServlet {
    private final AuditLogService auditLogService = new AuditLogService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!PermissionAccess.require(request, response, Permissions.VIEW_AUDIT_LOG)) return;
        try {
            AuditLogPage auditPage = auditLogService.findEntries(request.getParameter("action"), request.getParameter("actor"),
                    request.getParameter("targetType"), request.getParameter("successful"), request.getParameter("page"));
            request.setAttribute("auditPage", auditPage);
            request.getRequestDispatcher("/WEB-INF/views/audit-log.jsp").forward(request, response);
        } catch (SQLException exception) {
            throw new ServletException("Could not load audit log", exception);
        }
    }
}
