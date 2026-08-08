package com.learning.servlet;

import com.learning.dao.HibernateUserDao;
import com.learning.dao.UserDao;
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
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute(LoginServlet.LOGGED_IN_USER_ID) instanceof Long loggedInUserId)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Long userId = userId(request.getParameter("userId"));
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        try {
            if (!userDao.deleteById(userId)) {
                response.sendRedirect(request.getContextPath() + "/home");
                return;
            }

            if (userId.equals(loggedInUserId)) {
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            response.sendRedirect(request.getContextPath() + "/home?message=userDeleted");
        } catch (SQLException exception) {
            throw new ServletException("Could not delete user", exception);
        }
    }

    private Long userId(String value) {
        try {
            long parsed = Long.parseLong(value);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
