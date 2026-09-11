package com.learning.filter;

import com.learning.api.ApiResponses;
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
import java.util.Set;

/** Applies session authentication and forced-password-change rules to protected JSON endpoints. */
@WebFilter("/api/*")
public class ApiAuthenticationFilter implements Filter {
    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "/api/auth/login", "/api/auth/register", "/api/auth/forgot-password", "/api/auth/reset-password");
    private final UserDao userDao = new HibernateUserDao();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String route = request.getRequestURI().substring(request.getContextPath().length());
        if (PUBLIC_ENDPOINTS.contains(route)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        Object sessionUserId = session == null ? null : session.getAttribute(LoginServlet.LOGGED_IN_USER_ID);
        if (!(sessionUserId instanceof Long userId)) {
            ApiResponses.error(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHENTICATED", "Authentication is required.");
            return;
        }

        try {
            Optional<User> found = userDao.findById(userId);
            if (found.isEmpty()) {
                session.invalidate();
                ApiResponses.error(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHENTICATED", "Authentication is required.");
                return;
            }
            User user = found.get();
            boolean changeRequired = user.isMustChangePassword() || PasswordPolicy.isExpired(user);
            boolean allowedWhileChanging = "/api/users/me/password".equals(route) || "/api/auth/logout".equals(route);
            if (changeRequired && !allowedWhileChanging) {
                ApiResponses.error(response, HttpServletResponse.SC_FORBIDDEN, "PASSWORD_CHANGE_REQUIRED",
                        "You must change your password before using this endpoint.");
                return;
            }
            request.setAttribute("signedInUser", user);
            chain.doFilter(request, response);
        } catch (SQLException exception) {
            ApiResponses.error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                    "The request could not be completed.");
        }
    }
}
