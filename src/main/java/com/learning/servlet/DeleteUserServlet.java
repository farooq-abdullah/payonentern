package com.learning.servlet;

import com.learning.dao.HibernateUserDao;
import com.learning.dao.UserDao;
import com.learning.util.AdminAccess;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/delete-user")
public class DeleteUserServlet extends HttpServlet {
    private final UserDao userDao = new HibernateUserDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AdminAccess.requireAdmin(request, response)) {
            return;
        }

        Long userId = parseUserId(request.getParameter("userId"));
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        try {
            if (!userDao.deleteById(userId)) {
                response.sendRedirect(request.getContextPath() + "/home");
                return;
            }

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

    private Long parseUserId(String value) {
        try {
            long parsed = Long.parseLong(value);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException | NullPointerException exception) {
            return null;
        }
    }
}
