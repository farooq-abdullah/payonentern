package com.learning.servlet;

import com.learning.model.User;
import com.learning.service.ServiceResult;
import com.learning.service.UserManagementService;
import com.learning.util.PermissionAccess;
import com.learning.util.Permissions;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/delete-user")
public class DeleteUserServlet extends HttpServlet {
    private final UserManagementService userManagementService = new UserManagementService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!PermissionAccess.require(request, response, Permissions.DELETE_USER)) return;
        Long userId = parseId(request.getParameter("userId"));
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        try {
            ServiceResult<User> result = userManagementService.delete((User) request.getAttribute("signedInUser"), userId);
            if (!result.successful()) {
                String message = result.status() == ServiceResult.Status.PROTECTED ? "lastAdminProtected" : "userNotFound";
                response.sendRedirect(request.getContextPath() + "/home?message=" + message);
                return;
            }

            HttpSession session = request.getSession(false);
            if (session != null && userId.equals(session.getAttribute(LoginServlet.LOGGED_IN_USER_ID))) {
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/home?message=userDeleted");
        } catch (SQLException exception) {
            throw new ServletException("Could not delete user", exception);
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
