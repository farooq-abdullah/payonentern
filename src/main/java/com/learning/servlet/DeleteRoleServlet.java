package com.learning.servlet;

import com.learning.dao.HibernateRoleDao;
import com.learning.dao.HibernateUserDao;
import com.learning.dao.RoleDao;
import com.learning.dao.UserDao;
import com.learning.model.Role;
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

@WebServlet("/delete-role")
public class DeleteRoleServlet extends HttpServlet {
    private final RoleDao roleDao = new HibernateRoleDao();
    private final UserDao userDao = new HibernateUserDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!PermissionAccess.require(request, response, Permissions.MANAGE_ROLES)) {
            return;
        }

        Long roleId = parseId(request.getParameter("roleId"));
        if (roleId == null) {
            response.sendRedirect(request.getContextPath() + "/roles");
            return;
        }

        try {
            Optional<Role> found = roleDao.findById(roleId);
            if (found.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/roles");
                return;
            }
            if (found.get().isDefaultRole()) {
                response.sendRedirect(request.getContextPath() + "/roles?message=defaultRoleProtected");
                return;
            }
            if (userDao.countByRoleId(roleId) > 0) {
                response.sendRedirect(request.getContextPath() + "/roles?message=roleInUse");
                return;
            }

            roleDao.deleteById(roleId);
            response.sendRedirect(request.getContextPath() + "/roles?message=roleDeleted");
        } catch (SQLException exception) {
            throw new ServletException("Could not delete role", exception);
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
