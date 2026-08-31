package com.learning.servlet;

import com.learning.service.AuthenticationResult;
import com.learning.service.AuthenticationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    public static final String LOGGED_IN_USER_ID = "loggedInUserId";
    public static final String LOGGED_IN_USERNAME = "loggedInUsername";

    private final AuthenticationService authenticationService = new AuthenticationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(LOGGED_IN_USER_ID) != null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        request.setAttribute("username", username);

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            request.setAttribute("error", "Username and password are required.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        try {
            AuthenticationResult result = authenticationService.authenticate(username.trim(), password);
            if (result.status() == AuthenticationResult.Status.LOCKED) {
                request.setAttribute("error", "This account is temporarily locked after too many failed attempts. Ask an administrator to unlock it or try again later.");
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
                return;
            }
            if (result.status() != AuthenticationResult.Status.SUCCESS) {
                request.setAttribute("error", "Invalid username or password.");
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
                return;
            }

            var user = result.user();
            HttpSession session = request.getSession();
            session.setAttribute(LOGGED_IN_USER_ID, user.getId());
            session.setAttribute(LOGGED_IN_USERNAME, user.getUsername());
            response.sendRedirect(request.getContextPath() + "/home");
        } catch (java.sql.SQLException exception) {
            throw new ServletException("Could not log in", exception);
        }
    }
}
