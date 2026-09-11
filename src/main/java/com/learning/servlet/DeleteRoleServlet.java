package com.learning.servlet;

import com.learning.model.Role;
import com.learning.model.User;
import com.learning.service.RoleManagementService;
import com.learning.service.ServiceResult;
import com.learning.util.PermissionAccess;
import com.learning.util.Permissions;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/delete-role")
public class DeleteRoleServlet extends HttpServlet {
    private final RoleManagementService roleManagementService = new RoleManagementService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!PermissionAccess.require(request, response, Permissions.MANAGE_ROLES)) return;
        Long roleId = parseId(request.getParameter("roleId"));
        if (roleId == null) {
            response.sendRedirect(request.getContextPath() + "/roles");
            return;
        }
        try {
            ServiceResult<Role> result = roleManagementService.delete((User) request.getAttribute("signedInUser"), roleId);
            if (!result.successful()) {
                String message = result.status() == ServiceResult.Status.CONFLICT ? "roleInUse"
                        : result.status() == ServiceResult.Status.PROTECTED ? "defaultRoleProtected" : "roleNotFound";
                response.sendRedirect(request.getContextPath() + "/roles?message=" + message);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/roles?message=roleDeleted");
        } catch (SQLException exception) {
            throw new ServletException("Could not delete role", exception);
        }
    }

    private Long parseId(String value) {
        try {
            long parsed = Long.parseLong(value);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException | NullPointerException exception) {
            return null;
        }
    }
}
