package com.learning.servlet;

import com.learning.dao.HibernateUserDao;
import com.learning.dao.UserDao;
import com.learning.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/edit-user")
public class EditUserServlet extends HttpServlet {
    private final UserDao userDao = new HibernateUserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Long userId = userId(request.getParameter("id"));
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        try {
            Optional<User> user = userDao.findById(userId);
            if (user.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/home");
                return;
            }
            request.setAttribute("user", user.get());
            showForm(request, response);
        } catch (SQLException exception) {
            throw new ServletException("Could not load user", exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Long userId = userId(request.getParameter("userId"));
        String username = trimmedParameter(request, "username");
        String email = trimmedParameter(request, "email");

        if (userId == null || username.isEmpty() || email.isEmpty() || username.length() > 50 || email.length() > 254) {
            request.setAttribute("error", "Enter a valid username and email.");
            request.setAttribute("user", formUser(userId, username, email));
            showForm(request, response);
            return;
        }

        try {
            if (userDao.existsByUsernameOrEmailExceptId(username, email, userId)) {
                request.setAttribute("error", "That username or email is already registered.");
                request.setAttribute("user", formUser(userId, username, email));
                showForm(request, response);
                return;
            }

            User user = formUser(userId, username, email);
            if (!userDao.updateProfile(user)) {
                response.sendRedirect(request.getContextPath() + "/home");
                return;
            }

            HttpSession session = request.getSession(false);
            if (session != null && userId.equals(session.getAttribute(LoginServlet.LOGGED_IN_USER_ID))) {
                session.setAttribute(LoginServlet.LOGGED_IN_USERNAME, username);
            }
            response.sendRedirect(request.getContextPath() + "/home?message=profileUpdated");
        } catch (SQLException exception) {
            throw new ServletException("Could not update user", exception);
        }
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(LoginServlet.LOGGED_IN_USER_ID) instanceof Long;
    }

    private Long userId(String value) {
        try {
            long parsed = Long.parseLong(value);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String trimmedParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private User formUser(Long userId, String username, String email) {
        User user = new User();
        if (userId != null) {
            user.setId(userId);
        }
        user.setUsername(username);
        user.setEmail(email);
        return user;
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/edit-user.jsp").forward(request, response);
    }
}
