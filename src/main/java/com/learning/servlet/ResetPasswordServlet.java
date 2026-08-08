package com.learning.servlet;

import com.learning.dao.HibernateUserDao;
import com.learning.dao.UserDao;
import com.learning.model.User;
import com.learning.util.AdminAccess;
import com.learning.util.PasswordHasher;
import com.learning.util.PasswordPolicy;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@WebServlet("/reset-password")
public class ResetPasswordServlet extends HttpServlet {
    private final UserDao userDao = new HibernateUserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AdminAccess.requireAdmin(request, response)) {
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
        if (!AdminAccess.requireAdmin(request, response)) {
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

            if (newPassword == null || newPassword.isBlank()
                    || confirmation == null || confirmation.isBlank()) {
                error(request, response, "New password and confirmation are required.");
                return;
            }
            if (!newPassword.equals(confirmation)) {
                error(request, response, "New password and confirmation do not match.");
                return;
            }

            String passwordError = PasswordPolicy.validationError(newPassword);
            if (passwordError != null) {
                error(request, response, passwordError);
                return;
            }

            List<String> recentHashes = userDao.findRecentPasswordHashes(userId, 4);
            if (PasswordPolicy.matchesCurrentOrRecentPassword(newPassword, user, recentHashes)) {
                error(request, response, "New password cannot match the user's current or previous four passwords.");
                return;
            }

            userDao.updatePassword(userId, PasswordHasher.hash(newPassword), true);
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
