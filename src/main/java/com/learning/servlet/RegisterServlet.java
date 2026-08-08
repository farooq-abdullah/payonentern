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

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private final UserDao userDao = new HibernateUserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        showForm(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = trimmedParameter(request, "username");
        String email = trimmedParameter(request, "email");
        String password = request.getParameter("password");

        request.setAttribute("username", username);
        request.setAttribute("email", email);

        if (username.isEmpty() || email.isEmpty() || password == null || password.isBlank()) {
            request.setAttribute("error", "Username, email, and password are required.");
            showForm(request, response);
            return;
        }

        if (username.length() > 50 || email.length() > 254) {
            request.setAttribute("error", "Enter a valid username and email.");
            showForm(request, response);
            return;
        }

        String passwordError = PasswordPolicy.validationError(password);
        if (passwordError != null) {
            request.setAttribute("error", passwordError);
            showForm(request, response);
            return;
        }

        try {
            if (userDao.existsByUsernameOrEmail(username, email)) {
                request.setAttribute("error", "That username or email is already registered.");
                showForm(request, response);
                return;
            }

            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPasswordHash(PasswordHasher.hash(password));
            userDao.create(user);

            response.sendRedirect(request.getContextPath() + "/login?registered=true");
        } catch (SQLException exception) {
            throw new ServletException("Could not create account", exception);
        }
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    private String trimmedParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }
}
