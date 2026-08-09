package com.learning.servlet;

import com.learning.dao.HibernateUserDao;
import com.learning.dao.UserDao;
import com.learning.model.User;
import com.learning.util.PasswordHasher;
import com.learning.util.PasswordPolicy;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/change-password")
public class ChangePasswordServlet extends HttpServlet {
    private final UserDao userDao = new HibernateUserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (loggedInUserId(request) == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        showForm(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long userId = loggedInUserId(request);
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmation = request.getParameter("confirmation");

        if (isBlank(currentPassword) || isBlank(newPassword) || isBlank(confirmation)) {
            error(request, response, "All password fields are required.");
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

        try {
            Optional<User> found = userDao.findById(userId);
            if (found.isEmpty()) {
                request.getSession().invalidate();
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            User user = found.get();
            if (!PasswordHasher.matches(currentPassword, user.getPasswordHash())) {
                error(request, response, "Current password is incorrect.");
                return;
            }

            if (PasswordHasher.matches(newPassword, user.getPasswordHash())) {
                error(request, response, "New password must be different from your current password.");
                return;
            }

            userDao.updatePassword(userId, PasswordHasher.hash(newPassword), false);
            response.sendRedirect(request.getContextPath() + "/home?message=passwordChanged");
        } catch (SQLException exception) {
            throw new ServletException("Could not change password", exception);
        }
    }

    private Long loggedInUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute(LoginServlet.LOGGED_IN_USER_ID);
        return value instanceof Long userId ? userId : null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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
