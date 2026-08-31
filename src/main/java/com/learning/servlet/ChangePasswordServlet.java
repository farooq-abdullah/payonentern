package com.learning.servlet;

import com.learning.service.PasswordManagementService;
import com.learning.service.PasswordOperationResult;
import com.learning.util.PermissionAccess;
import com.learning.util.Permissions;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/change-password")
public class ChangePasswordServlet extends HttpServlet {
    private final PasswordManagementService passwordManagementService = new PasswordManagementService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!PermissionAccess.require(request, response, Permissions.CHANGE_OWN_PASSWORD)) {
            return;
        }
        showForm(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!PermissionAccess.require(request, response, Permissions.CHANGE_OWN_PASSWORD)) {
            return;
        }
        Long userId = loggedInUserId(request);
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmation = request.getParameter("confirmation");

        try {
            if (userId == null) {
                request.getSession().invalidate();
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            PasswordOperationResult result = passwordManagementService.changeOwnPassword(userId, currentPassword, newPassword, confirmation);
            if (!result.successful()) {
                error(request, response, result.error());
                return;
            }
            response.sendRedirect(request.getContextPath() + "/home?message=passwordChanged");
        } catch (java.sql.SQLException exception) {
            throw new ServletException("Could not change password", exception);
        }
    }

    private Long loggedInUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute(LoginServlet.LOGGED_IN_USER_ID);
        return value instanceof Long userId ? userId : null;
    }

    private void error(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        showForm(request, response);
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/change-password.jsp").forward(request, response);
    }
}
