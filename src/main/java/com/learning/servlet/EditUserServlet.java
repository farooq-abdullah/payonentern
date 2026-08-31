package com.learning.servlet;

import com.learning.dao.HibernateRoleDao;
import com.learning.dao.HibernateUserDao;
import com.learning.dao.RoleDao;
import com.learning.dao.UserDao;
import com.learning.model.Role;
import com.learning.model.User;
import com.learning.util.FullAdminProtection;
import com.learning.util.PermissionAccess;
import com.learning.util.Permissions;
import com.learning.util.UserInputValidator;
import com.learning.service.AuditService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;

@WebServlet("/edit-user")
public class EditUserServlet extends HttpServlet {
    private final UserDao userDao = new HibernateUserDao();
    private final RoleDao roleDao = new HibernateRoleDao();
    private final AuditService auditService = new AuditService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!PermissionAccess.require(request, response, Permissions.EDIT_USER)) {
            return;
        }

        Long userId = parseId(request.getParameter("id"));
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
        if (!PermissionAccess.require(request, response, Permissions.EDIT_USER)) {
            return;
        }

        Long userId = parseId(request.getParameter("userId"));
        Long roleId = parseId(request.getParameter("roleId"));
        String username = trimmedParameter(request, "username");
        String email = trimmedParameter(request, "email");

        if (userId == null || roleId == null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        try {
            Optional<User> foundUser = userDao.findById(userId);
            Optional<Role> foundRole = roleDao.findById(roleId);
            if (foundUser.isEmpty() || foundRole.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/home");
                return;
            }

            String userInputError = UserInputValidator.validationError(username, email);
            if (userInputError != null) {
                request.setAttribute("error", userInputError);
                request.setAttribute("user", formUser(userId, username, email, foundRole.get()));
                showForm(request, response);
                return;
            }

            if (userDao.existsByUsernameOrEmailExceptId(username, email, userId)) {
                request.setAttribute("error", "That username or email is already registered.");
                request.setAttribute("user", formUser(userId, username, email, foundRole.get()));
                showForm(request, response);
                return;
            }

            Set<String> allFunctionCodes = roleDao.findAllFunctionCodes();
            boolean losesFullAdministration = FullAdminProtection.isFullAdministrator(foundUser.get().getRole(), allFunctionCodes)
                    && !FullAdminProtection.isFullAdministrator(foundRole.get(), allFunctionCodes);
            if (losesFullAdministration && FullAdminProtection.countFullAdministrators(roleDao, userDao) <= 1) {
                request.setAttribute("error", "At least one user must keep full administrative permissions.");
                request.setAttribute("user", formUser(userId, username, email, foundRole.get()));
                showForm(request, response);
                return;
            }

            User user = formUser(userId, username, email, foundRole.get());
            userDao.updateProfile(user);
            auditService.record((User) request.getAttribute("signedInUser"), "USER_UPDATED", "USER", userId,
                    username, true, "Role set to " + foundRole.get().getName());

            HttpSession session = request.getSession(false);
            if (session != null && userId.equals(session.getAttribute(LoginServlet.LOGGED_IN_USER_ID))) {
                session.setAttribute(LoginServlet.LOGGED_IN_USERNAME, username);
            }
            response.sendRedirect(request.getContextPath() + "/home?message=profileUpdated");
        } catch (SQLException exception) {
            throw new ServletException("Could not update user", exception);
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

    private String trimmedParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private User formUser(long userId, String username, String email, Role role) {
        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("roles", roleDao.findAll());
        } catch (SQLException exception) {
            throw new ServletException("Could not load roles", exception);
        }
        request.getRequestDispatcher("/WEB-INF/views/edit-user.jsp").forward(request, response);
    }
}
