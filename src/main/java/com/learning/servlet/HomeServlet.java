package com.learning.servlet;

import com.learning.dao.HibernateUserDao;
import com.learning.dao.UserDao;
import com.learning.model.User;
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
    private final UserDao userDao = new HibernateUserDao();

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
            request.setAttribute("canManageRoles", PermissionAccess.hasPermission(signedInUser, Permissions.MANAGE_ROLES));
            request.setAttribute("users", userDao.findAll());
        } catch (SQLException exception) {
            throw new ServletException("Could not load users", exception);
        }

        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}
