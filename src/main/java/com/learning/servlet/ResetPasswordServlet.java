package com.learning.servlet;

import com.learning.dao.UserDao;
import com.learning.dao.HibernateUserDao;
import com.learning.model.User;
import com.learning.service.PasswordManagementService;
import com.learning.service.PasswordOperationResult;
import com.learning.util.PermissionAccess;
import com.learning.util.Permissions;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/reset-password")
public class ResetPasswordServlet extends HttpServlet {
    private final UserDao userDao = new HibernateUserDao();
    private final PasswordManagementService passwordManagementService = new PasswordManagementService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!PermissionAccess.require(request, response, Permissions.RESET_PASSWORD)) {
            return;
        }

        Long userId = parseUserId(request.getParameter("id"));
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        try {
            Optional<User> found = userDao.findById(userId);
            if (found.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/home");
                return;
            }
            request.setAttribute("user", found.get());
            showForm(request, response);
        } catch (SQLException exception) {
            throw new ServletException("Could not load user", exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!PermissionAccess.require(request, response, Permissions.RESET_PASSWORD)) {
            return;
        }

        Long userId = parseUserId(request.getParameter("userId"));
        String newPassword = request.getParameter("newPassword");
        String confirmation = request.getParameter("confirmation");

        try {
            Optional<User> found = userId == null ? Optional.empty() : userDao.findById(userId);
            if (found.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/home");
                return;
            }

            User user = found.get();
            request.setAttribute("user", user);

            PasswordOperationResult result = passwordManagementService.resetByAdministrator(
                    (User) request.getAttribute("signedInUser"), userId, newPassword, confirmation);
            if (!result.successful()) {
                error(request, response, result.error());
                return;
            }
            response.sendRedirect(request.getContextPath() + "/home?message=passwordReset");
        } catch (SQLException exception) {
            throw new ServletException("Could not reset password", exception);
        }
    }

    private Long parseUserId(String value) {
        try {
            long parsed = Long.parseLong(value);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException | NullPointerException exception) {
            return null;
        }
    }

    private void error(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        showForm(request, response);
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/reset-password.jsp").forward(request, response);
    }
}
