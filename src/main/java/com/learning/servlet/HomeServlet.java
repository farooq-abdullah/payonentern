package com.learning.servlet;

import com.learning.dao.HibernateUserDao;
import com.learning.dao.UserDao;
import com.learning.model.User;
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

@WebServlet("/home")
public class HomeServlet extends HttpServlet {
    private final UserDao userDao = new HibernateUserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session == null || !(session.getAttribute(LoginServlet.LOGGED_IN_USER_ID) instanceof Long userId)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            Optional<User> signedInUser = userDao.findById(userId);
            if (signedInUser.isEmpty()) {
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            if (signedInUser.get().isMustChangePassword() || PasswordPolicy.isExpired(signedInUser.get())) {
                response.sendRedirect(request.getContextPath() + "/change-password?required=true");
                return;
            }

            request.setAttribute("isAdmin", signedInUser.get().isAdmin());
            request.setAttribute("users", userDao.findAll());
        } catch (SQLException exception) {
            throw new ServletException("Could not load users", exception);
        }

        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}
