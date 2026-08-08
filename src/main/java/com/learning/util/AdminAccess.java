package com.learning.util;

import com.learning.servlet.LoginServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public final class AdminAccess {
    private AdminAccess() {
    }

    public static boolean requireAdmin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(LoginServlet.LOGGED_IN_USER_ID) == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        if (!"ADMIN".equals(session.getAttribute(LoginServlet.LOGGED_IN_ROLE))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }
}
