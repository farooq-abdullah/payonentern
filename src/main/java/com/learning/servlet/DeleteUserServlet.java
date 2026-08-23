package com.learning.servlet;

import com.learning.dao.HibernateRoleDao;
import com.learning.dao.HibernateUserDao;
import com.learning.dao.RoleDao;
import com.learning.dao.UserDao;
import com.learning.model.User;
import com.learning.util.FullAdminProtection;
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
import java.util.Optional;

@WebServlet("/delete-user")
public class DeleteUserServlet extends HttpServlet {
    private final UserDao userDao = new HibernateUserDao();
    private final RoleDao roleDao = new HibernateRoleDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!PermissionAccess.require(request, response, Permissions.DELETE_USER)) {
            return;
        }

        Long userId = parseId(request.getParameter("userId"));
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

            if (FullAdminProtection.isFullAdministrator(found.get().getRole(), roleDao.findAllFunctionCodes())
                    && FullAdminProtection.countFullAdministrators(roleDao, userDao) <= 1) {
                response.sendRedirect(request.getContextPath() + "/home?message=lastAdminProtected");
                return;
            }

            userDao.deleteById(userId);

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
