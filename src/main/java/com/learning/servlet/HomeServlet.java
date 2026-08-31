package com.learning.servlet;

import com.learning.dao.UserPage;
import com.learning.model.User;
import com.learning.service.UserDirectoryService;
import com.learning.util.PermissionAccess;
import com.learning.util.Permissions;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {
    private final UserDirectoryService userDirectoryService = new UserDirectoryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!PermissionAccess.require(request, response, Permissions.VIEW_USERS)) {
            return;
        }
        try {
            User signedInUser = (User) request.getAttribute("signedInUser");
            request.setAttribute("canChangeOwnPassword", PermissionAccess.hasPermission(
                    signedInUser, Permissions.CHANGE_OWN_PASSWORD));
            request.setAttribute("canEditUser", PermissionAccess.hasPermission(signedInUser, Permissions.EDIT_USER));
            request.setAttribute("canDeleteUser", PermissionAccess.hasPermission(signedInUser, Permissions.DELETE_USER));
            request.setAttribute("canResetPassword", PermissionAccess.hasPermission(signedInUser, Permissions.RESET_PASSWORD));
            request.setAttribute("canUnlockUser", PermissionAccess.hasPermission(signedInUser, Permissions.UNLOCK_USER));
            request.setAttribute("canManageRoles", PermissionAccess.hasPermission(signedInUser, Permissions.MANAGE_ROLES));
            request.setAttribute("canViewAuditLog", PermissionAccess.hasPermission(signedInUser, Permissions.VIEW_AUDIT_LOG));
            UserPage userPage = userDirectoryService.findUsers(request.getParameter("search"), request.getParameter("sort"),
                    request.getParameter("dir"), request.getParameter("page"));
            request.setAttribute("userPage", userPage);
            request.setAttribute("users", userPage.users());
        } catch (SQLException exception) {
            throw new ServletException("Could not load users", exception);
        }

        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}
