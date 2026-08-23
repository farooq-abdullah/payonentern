package com.learning.filter;

import com.learning.dao.HibernateUserDao;
import com.learning.dao.UserDao;
import com.learning.model.User;
import com.learning.servlet.LoginServlet;
import com.learning.util.PasswordPolicy;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebFilter(urlPatterns = {
        "/home",
        "/change-password",
        "/edit-user",
        "/delete-user",
        "/reset-password",
        "/roles",
        "/edit-role",
        "/delete-role"
})
public class ProtectedEndpointFilter implements Filter {
    private final UserDao userDao = new HibernateUserDao();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute(LoginServlet.LOGGED_IN_USER_ID) instanceof Long userId)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            Optional<User> found = userDao.findById(userId);
            if (found.isEmpty()) {
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            User user = found.get();
            boolean changeRequired = user.isMustChangePassword() || PasswordPolicy.isExpired(user);
            if (changeRequired && !"/change-password".equals(request.getServletPath())) {
                response.sendRedirect(request.getContextPath() + "/change-password?required=true");
                return;
            }

            request.setAttribute("signedInUser", user);
            chain.doFilter(request, response);
        } catch (SQLException exception) {
            throw new ServletException("Could not verify the signed-in user", exception);
        }
    }
}
