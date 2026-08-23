package com.learning.servlet;

import com.learning.dao.HibernateRoleDao;
import com.learning.dao.HibernateUserDao;
import com.learning.dao.RoleDao;
import com.learning.dao.UserDao;
import com.learning.model.Role;
import com.learning.model.User;
import com.learning.util.FullAdminProtection;
import com.learning.util.PasswordHasher;
import com.learning.util.PasswordPolicy;
import com.learning.util.UserInputValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private final UserDao userDao = new HibernateUserDao();
    private final RoleDao roleDao = new HibernateRoleDao();

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

        if (password == null || password.isBlank()) {
            request.setAttribute("error", "Username, email, and password are required.");
            showForm(request, response);
            return;
        }

        String userInputError = UserInputValidator.validationError(username, email);
        if (userInputError != null) {
            request.setAttribute("error", userInputError);
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
            Optional<Role> role = userDao.countAll() == 0
                    ? FullAdminProtection.findFullAdministratorRole(roleDao)
                    : roleDao.findDefaultRole();
            if (role.isEmpty()) {
                throw new ServletException("No role is available for new users.");
            }
            user.setRole(role.get());
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
